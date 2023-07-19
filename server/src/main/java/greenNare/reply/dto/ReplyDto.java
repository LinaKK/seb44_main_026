package greenNare.reply.dto;

import greenNare.reply.entity.Reply;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

import javax.persistence.Column;

@NoArgsConstructor
//@AllArgsConstructor
@Getter
@Builder
public class ReplyDto {
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Response {
        private long replyId;
        private long memberId;
        private long challengeId;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        /*
        private String name;
        private int point;

        public void setName(String name) {
            this.name = name;
        }

        public void setPoint(int point) {
            this.point = point;
        }

         */
        public static Response from(Reply reply) {// Member member)
            return Response.builder()
                    .replyId(reply.getReplyId())
                    .memberId(reply.getMemberId())
                    .challengeId(reply.getChallengeId())
                    .content(reply.getContent())
                    //.name(member.getName())
                    //.point(member.getPoint())
                    .build();
        }

    }

    @Getter
    @Setter
    public static class Post {
        private String content;
    }
}
