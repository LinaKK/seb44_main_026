package greenNare.product.service;

import greenNare.auth.jwt.JwtTokenizer;
import greenNare.exception.BusinessLogicException;
import greenNare.exception.ExceptionCode;
import greenNare.member.repository.MemberRepository;
import greenNare.member.service.MemberService;
import greenNare.product.dto.GetReviewWithImageDto;
import greenNare.product.entity.Image;
import greenNare.product.entity.Review;
import greenNare.product.repository.ImageRepository;
import greenNare.product.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReviewService {
    private ReviewRepository reviewRepository;

    private ImageRepository imageRepository;    //imageService를 만들어야할지

    private ProductService productService;

    private MemberRepository memberRepository;

    private MemberService memberService;

    public static final String IMAGE_SAVE_URL = "/home/ssm-user/seb44_main_026/images/";

    public static final String SEPERATOR  = "_";


    public ReviewService(ReviewRepository reviewRepository,
                         ProductService productService,
                         MemberRepository memberRepository,
                         ImageRepository imageRepository,
                         MemberService memberService) {

        this.reviewRepository = reviewRepository;
        this.productService = productService;
        this.memberRepository = memberRepository;
        this.imageRepository = imageRepository;
        this.memberService = memberService;
    }


    public Page<Review> getReviews(int productId, PageRequest pageable) {
        //PageRequest pageable = PageRequest.of(page,size);
        Page<Review> reviews = reviewRepository.findByProductProductId(pageable, productId);

        return reviews;
    }


    public Page<Review> getMyReviews(int memberId, PageRequest pageable) {
        Page<Review> reviews = reviewRepository.findByMemberMemberId(memberId, pageable);
        return reviews;
    }

    public List<GetReviewWithImageDto> getReviewImage(Page<Review> reviews) {
        List<GetReviewWithImageDto> getReviewWithImageDtos = reviews.getContent().stream()
                .map(review -> {
                    List<Image> images = imageRepository.findImagesUriByReviewReviewId(review.getReviewId());
                    List<String> imageLinks = images.stream()
                            .map(image -> image.getImageUri())
                            .collect(Collectors.toList());

                    GetReviewWithImageDto resultDto = new GetReviewWithImageDto(
                            review.getMember().getMemberId(),
                            review.getReviewId(),
                            review.getContext(),
                            review.getCreatedAt(),
                            review.getUpdatedAt(),
                            review.getProduct().getProductId(),
                            imageLinks,
                            review.getMember().getName(),
                            review.getMember().getPoint()
                    );
//                    resultDto.setReviewId(review.getReviewId());
//                    resultDto.setContext(review.getContext());
//                    resultDto.setCreatedAt(review.getCreatedAt());
//                    resultDto.setUpdateId(review.getUpdatedAt());
//                    resultDto.setProductId(review.getProduct().getProductId());
//                    resultDto.setImageLinks(images);
//                    resultDto.setName(review.getMember().getName());
//                    resultDto.setPoint(review.getMember().getPoint());
                    return resultDto;
                })
                .collect(Collectors.toList());

        return getReviewWithImageDtos;
    }


//    create결과 리턴?
    public void createReview(Review review, int memberId, int productId) {
        //
        verifyExistsReview(memberId, productId);

        review.setMember(memberRepository.findBymemberId(memberId));
        review.setProduct(productService.getProduct(productId));
        reviewRepository.save(review);

        System.out.println("createReview " + review);

        //updatePoint(response-변경된 포인트 전송)
        int point = (int)Math.floor(review.getProduct().getPrice() * 0.01);
        memberService.addPoint(memberId, point);
    }


    public void updateReview(Review review, List<String> deleteImages, int memberId, int productId) {
        //
        Review findReview = findReview(memberId, productId);

        findReview.setContext(review.getContext());
        findReview.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(findReview);
        System.out.println("updateReview " + review);

        for(int i = 0; i<deleteImages.size(); i++) {
            if(imageRepository.findImageUriByImageUri(deleteImages.get(i)).isPresent()){
                Image ig = imageRepository.findImageUriByImageUri(deleteImages.get(i)).orElseThrow();
                imageRepository.delete(ig);
            }
        }

    }


    public void createReviewWithImage(Review review, List<MultipartFile> images, int memberId, int productId) {
        //
        verifyExistsReview(memberId, productId);

        review.setMember(memberRepository.findBymemberId(memberId));
        review.setProduct(productService.getProduct(productId));
        reviewRepository.save(review);

        System.out.println("createReview " + review);
        if(images.size() !=0){
            log.info("images_exist");
            List<Image> saveImages = images.stream().map(
                    image -> {
                        try {
                            return imageRepository.save(new Image(createImageName(image), findReview(memberId,productId)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            ).collect(Collectors.toList());
        }

        //updatePoint(response-변경된 포인트 전송)
        int point = (int)Math.floor(review.getProduct().getPrice() * 0.01);
        memberService.addPoint(memberId, point);
    }


    public void updateReviewWithImage(Review review, List<String> deleteImages, List<MultipartFile> images, int memberId, int productId) {
        //
        Review findReview = findReview(memberId, productId);

        findReview.setContext(review.getContext());
        findReview.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(findReview);

        for(int i = 0; i<deleteImages.size(); i++) {
            if(imageRepository.findImageUriByImageUri(deleteImages.get(i)).isPresent()){
                Image ig = imageRepository.findImageUriByImageUri(deleteImages.get(i)).orElseThrow();
                imageRepository.delete(ig);
            }
        }

//        if(images != null && !images.isEmpty()){
//            List<Image> saveImages = images.stream().map(
//                    image -> {
//                        try {
//                            return imageRepository.save(new Image(createImageName(image), findReview(memberId,productId)));
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//            ).collect(Collectors.toList());
//        }
        List<Image> saveImages = images.stream().map(
                image -> {
                    try {
                        return imageRepository.save(new Image(createImageName(image), findReview(memberId,productId)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).collect(Collectors.toList());

        System.out.println("updateReview " + review);

    }




    public String createImageName(MultipartFile image) throws IOException{
        UUID uuid = UUID.randomUUID();
        String imageName = uuid + SEPERATOR + image.getOriginalFilename();

        File imagefile = new File(IMAGE_SAVE_URL, imageName);
        image.transferTo(imagefile);

        return "/images/" + imageName;

    }


    public void deleteReview(int memberId, int productId) {
        Review findReview = findReview(memberId, productId);

        List<Image> deleteImages = imageRepository.findByReviewReviewId(findReview.getReviewId());
        for(int i=0; i< deleteImages.size(); i++){
            imageRepository.delete(deleteImages.get(i));
        }

        reviewRepository.delete(findReview);
    }


    public void verifyExistsReview(int memberId, int productId) {
        boolean exist = reviewRepository.findByMemberMemberIdAndProductProductId(memberId, productId).isPresent();
        if(exist) throw new BusinessLogicException(ExceptionCode.REVIEW_EXIST);

    }


    public Review findReview(int memberId, int productId) {
        Optional<Review> optionalReview = reviewRepository.findByMemberMemberIdAndProductProductId(memberId, productId);
        Review findReview = optionalReview
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.REVIEW_NOT_FOUND));
        return findReview;
    }

}
