package greenNare.member.service;

import greenNare.cart.entity.Cart;
import greenNare.cart.entity.CartItem;
import greenNare.cart.repository.CartRepository;
import greenNare.cart.service.CartService;
import greenNare.config.SecurityConfiguration;
import greenNare.exception.BusinessLogicException;
import greenNare.exception.ExceptionCode;
import greenNare.member.entity.Member;
import greenNare.member.repository.MemberRepository;
import greenNare.product.dto.GetProductWithImageDto;
import greenNare.product.entity.Image;
import greenNare.product.entity.Product;
import greenNare.product.repository.ImageRepository;
import greenNare.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageReader;
import java.util.ArrayList;
import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class MemberService {
    private MemberRepository memberRepository;
    private SecurityConfiguration securityConfiguration;
    private ProductService productService;


    public MemberService(MemberRepository memberRepository,
                         SecurityConfiguration securityConfiguration,
                         ProductService productService) {
        this.memberRepository = memberRepository;
        this.securityConfiguration = securityConfiguration;
        this.productService = productService;
    }
    public Member loginMember(String email, String password) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        Member member = optionalMember.orElse(null);

        if (member != null && securityConfiguration.passwordEncoder().matches(password, member.getPassword())) {
            return member;
        }

        return null;
    }

    public Member createMember(Member member) {
        member.setPassword(securityConfiguration.passwordEncoder().encode(member.getPassword()));

        verifyExistsEmail(member.getEmail());

        return memberRepository.save(member);
    }

    public Member updateMember(Member member) {

        Member findMember = findVerifiedMember(member.getMemberId());


        Optional.ofNullable(member.getName())
                .ifPresent(name -> findMember.setName(name));

        return memberRepository.save(findMember);
    }


    public Member findMember(int memberId) {
        return findVerifiedMember(memberId);
    }

    public List<Member> findMember() {

        return (List<Member>) memberRepository.findAll();
    }


    public void deleteMember(int memberId) {
        Member findMember = findVerifiedMember(memberId);

        memberRepository.delete(findMember);
    }


    public Member findVerifiedMember(int memberId) {
        Optional<Member> optionalMember =
                memberRepository.findById(memberId);
        Member findMember =
                optionalMember.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
        return findMember;
    }


    private void verifyExistsEmail(String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isPresent())
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_EXIST));

    }
    public Member findMemberById(int memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(()-> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }

    public void addPoint(int memberId, int point) {
        Member member = findMemberById(memberId);
        int changePoint = member.getPoint() + point;
        member.setPoint(changePoint);
        memberRepository.save(member);
    }
    public void deletePoint(int memberId, int point) {
        Member member = findMemberById(memberId);
        int changePoint = member.getPoint() - point;
        if (changePoint < 0) {
            throw new BusinessLogicException(ExceptionCode.POINT_LAKE);
        }
        member.setPoint(changePoint);
        memberRepository.save(member);
    }



    //사용자 카트에 상품 추가
    public void addMyCart(int memberId, int productId) {
        CartItem item = new CartItem(productId);
        Member member = findMemberById(memberId);
        member.getCartItemList().add(item);
    }



    //사용자 카트에 담긴 상품 아이디 리스트 반환
    public List<Integer> getCartProductsId(int memberId) {
        Member member = findMemberById(memberId);
        log.info("member :" + member);

        log.info("cartProductSIds :" + member.getCartItemList());
        List<Integer> cartProductsId = member.getCartItemList().stream()
                .map(product -> {
                    return product.getProductId();
                }).collect(Collectors.toList());
        log.info("cartProductSIds :"+ cartProductsId);
        return cartProductsId;
    }



    //사용자 카트에 담긴 상품 객체 리스트 반환
    public List<GetProductWithImageDto> getCartProducts(List<Integer> productIds, Pageable pageRequest) {
        Page<Product> products = productService.getProducts(pageRequest, productIds);

        return productService.getProductsWithImage(products, true);

//        return productService.getProducts(productIds, pageRequest);
    }

}

