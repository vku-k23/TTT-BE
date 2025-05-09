package com.ttt.cinevibe.service;

import com.ttt.cinevibe.dto.response.UserSuggestionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserSuggestionService {

    /**
     * Gợi ý người dùng phù hợp để theo dõi dựa trên nhiều tiêu chí
     * @param userUid ID của người dùng cần gợi ý
     * @param pageable Thông tin phân trang
     * @return Danh sách người dùng được gợi ý
     */
    Page<UserSuggestionResponse> suggestPeople(String userUid, Pageable pageable);
    
    /**
     * Cập nhật trạng thái theo dõi của một gợi ý
     * @param userUid ID của người dùng
     * @param suggestedUserUid ID của người dùng được gợi ý
     * @param wasFollowed Đã theo dõi hay chưa
     */
    void updateSuggestionFollowStatus(String userUid, String suggestedUserUid, boolean wasFollowed);
}