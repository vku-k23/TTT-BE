package com.ttt.cinevibe.service.impl;

import com.ttt.cinevibe.dto.response.UserSuggestionResponse;
import com.ttt.cinevibe.exception.ResourceNotFoundException;
import com.ttt.cinevibe.model.User;
import com.ttt.cinevibe.model.UserConnection;
import com.ttt.cinevibe.model.UserSuggestionHistory;
import com.ttt.cinevibe.repository.MovieReviewRepository;
import com.ttt.cinevibe.repository.UserConnectionRepository;
import com.ttt.cinevibe.repository.UserRepository;
import com.ttt.cinevibe.repository.UserSuggestionHistoryRepository;
import com.ttt.cinevibe.service.UserSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSuggestionServiceImpl implements UserSuggestionService {

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final UserSuggestionHistoryRepository userSuggestionHistoryRepository;
    private final MovieReviewRepository movieReviewRepository;

    /**
     * Thuật toán gợi ý người dùng dựa trên nhiều tiêu chí kết hợp:
     * 1. Cùng thể loại phim yêu thích 
     * 2. Kết nối chung (bạn của bạn)
     * 3. Đánh giá phim tương tự
     * 4. Hoạt động nổi bật (người dùng tích cực)
     */
    @Override
    @Transactional
    public Page<UserSuggestionResponse> suggestPeople(String userUid, Pageable pageable) {
        User currentUser = userRepository.findById(userUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Lấy danh sách ID người dùng đã kết nối hoặc đã được gợi ý trước đó
        Set<String> excludedUserIds = new HashSet<>();
        excludedUserIds.add(userUid); // Loại trừ bản thân
        
        // Thêm người dùng đã follow
        userConnectionRepository.findByFollowerAndStatus(currentUser, UserConnection.ConnectionStatus.ACCEPTED)
                .forEach(conn -> excludedUserIds.add(conn.getFollowing().getFirebaseUid()));
        
        // Thêm người đã được gợi ý gần đây (trong 7 ngày)
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        userSuggestionHistoryRepository
                .findByUserAndCreatedAtAfterOrderByScoreDesc(currentUser, oneWeekAgo)
                .forEach(history -> excludedUserIds.add(history.getSuggestedUser().getFirebaseUid()));
        
        // 2. Tính điểm và lý do gợi ý cho từng người dùng
        List<User> allUsers = userRepository.findAll();
        Map<User, Double> userScores = new HashMap<>();
        Map<User, String> userReasons = new HashMap<>();
        
        for (User user : allUsers) {
            if (excludedUserIds.contains(user.getFirebaseUid())) {
                continue;
            }
            
            double score = 0;
            List<String> reasons = new ArrayList<>();
            
            // Tiêu chí 1: Cùng thể loại phim yêu thích
            if (currentUser.getFavoriteGenre() != null && user.getFavoriteGenre() != null) {
                if (currentUser.getFavoriteGenre().equals(user.getFavoriteGenre())) {
                    score += 5.0;
                    reasons.add("Cùng sở thích thể loại phim " + user.getFavoriteGenre());
                }
            }
            
            // Tiêu chí 2: Kết nối chung (nhiều người bạn theo dõi cũng theo dõi người này)
            int commonConnections = countCommonConnections(currentUser, user);
            if (commonConnections > 0) {
                score += Math.min(3.0, commonConnections * 0.5); // Tối đa 3 điểm
                reasons.add(commonConnections + " kết nối chung");
            }
            
            // Tiêu chí 3: Người dùng tích cực (nhiều đánh giá, nhiều người theo dõi)
            if (user.getReviewCount() != null && user.getReviewCount() > 10) {
                score += 2.0;
                reasons.add("Người dùng có nhiều đánh giá phim");
            }
            
            if (user.getFollowersCount() != null && user.getFollowersCount() > 50) {
                score += 2.0;
                reasons.add("Người dùng có nhiều người theo dõi");
            }
            
            // Nếu người dùng có điểm, thêm vào danh sách
            if (score > 0) {
                userScores.put(user, score);
                userReasons.put(user, String.join(", ", reasons));
            }
        }
        
        // 3. Sắp xếp theo điểm và chuyển đổi sang response
        List<User> suggestedUsers = userScores.entrySet().stream()
                .sorted(Map.Entry.<User, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 4. Phân trang kết quả
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), suggestedUsers.size());
        
        if (start >= suggestedUsers.size()) {
            return Page.empty();
        }
        
        List<User> pagedUsers = suggestedUsers.subList(start, end);
        
        // 5. Chuyển đổi sang response và lưu lịch sử gợi ý
        List<UserSuggestionResponse> responseList = new ArrayList<>();
        
        for (User suggestedUser : pagedUsers) {
            // Lưu lịch sử gợi ý nếu chưa tồn tại
            if (!userSuggestionHistoryRepository.existsByUserAndSuggestedUser(currentUser, suggestedUser)) {
                UserSuggestionHistory history = UserSuggestionHistory.builder()
                        .user(currentUser)
                        .suggestedUser(suggestedUser)
                        .reason(userReasons.get(suggestedUser))
                        .score(userScores.get(suggestedUser))
                        .wasFollowed(false)
                        .build();
                
                userSuggestionHistoryRepository.save(history);
            }
            
            UserSuggestionResponse response = UserSuggestionResponse.builder()
                    .userId(suggestedUser.getFirebaseUid())
                    .displayName(suggestedUser.getDisplayName())
                    .profileImageUrl(suggestedUser.getProfileImageUrl())
                    .bio(suggestedUser.getBio())
                    .favoriteGenre(suggestedUser.getFavoriteGenre())
                    .followersCount(suggestedUser.getFollowersCount())
                    .followingCount(suggestedUser.getFollowingCount())
                    .reviewCount(suggestedUser.getReviewCount())
                    .reasonForSuggestion(userReasons.get(suggestedUser))
                    .matchScore(userScores.get(suggestedUser))
                    .build();
            
            responseList.add(response);
        }
        
        return new PageImpl<>(responseList, pageable, suggestedUsers.size());
    }
    
    @Override
    @Transactional
    public void updateSuggestionFollowStatus(String userUid, String suggestedUserUid, boolean wasFollowed) {
        User user = userRepository.findById(userUid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        User suggestedUser = userRepository.findById(suggestedUserUid)
                .orElseThrow(() -> new ResourceNotFoundException("Suggested user not found"));
                
        userSuggestionHistoryRepository.findByUserOrderByScoreDesc(user)
            .stream()
            .filter(history -> history.getSuggestedUser().getFirebaseUid().equals(suggestedUserUid))
            .findFirst()
            .ifPresent(history -> {
                history.setWasFollowed(wasFollowed);
                userSuggestionHistoryRepository.save(history);
            });
    }
    
    /**
     * Đếm số lượng kết nối chung giữa hai người dùng
     */
    private int countCommonConnections(User user1, User user2) {
        // Lấy danh sách người dùng user1 đang theo dõi
        List<UserConnection> user1Following = userConnectionRepository.findByFollowerAndStatus(
                user1, UserConnection.ConnectionStatus.ACCEPTED);
        Set<String> user1FollowingIds = user1Following.stream()
                .map(conn -> conn.getFollowing().getFirebaseUid())
                .collect(Collectors.toSet());
        
        // Lấy danh sách người theo dõi user2
        List<UserConnection> user2Followers = userConnectionRepository.findByFollowingAndStatus(
                user2, UserConnection.ConnectionStatus.ACCEPTED);
        Set<String> user2FollowerIds = user2Followers.stream()
                .map(conn -> conn.getFollower().getFirebaseUid())
                .collect(Collectors.toSet());
        
        // Đếm số lượng ID chung
        int commonCount = 0;
        for (String id : user1FollowingIds) {
            if (user2FollowerIds.contains(id)) {
                commonCount++;
            }
        }
        
        return commonCount;
    }
}