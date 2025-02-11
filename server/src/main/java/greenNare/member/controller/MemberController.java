package greenNare.member.controller;

import greenNare.auth.jwt.JwtTokenizer;


import greenNare.Response.SingleResponseDto;
//import greenNare.cart.service.CartService;
import greenNare.member.entity.Member;
import greenNare.member.mapper.MemberMapper;
import greenNare.member.dto.MemberDto;
import greenNare.member.service.MemberService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

@RestController
   @RequestMapping("/user")
    public class MemberController {
    private final MemberMapper mapper;
    private final MemberService memberService;
    private final JwtTokenizer jwtTokenizer;

    public MemberController(MemberMapper mapper, MemberService memberService, JwtTokenizer jwtTokenizer) {
        this.mapper = mapper;
        this.memberService = memberService;
        this.jwtTokenizer = jwtTokenizer;
    }


    //회원가입
    @PostMapping("/join")
    public ResponseEntity postMember(@Valid @RequestBody MemberDto.Post requestBody) {
        Member member = mapper.memberPostToMember(requestBody);

        Member createdMember = memberService.createMember(member);
       /* URI location = UriCreator.createUri(MEMBER_DEFAULT_URL, createdMember.getMemberId());

        return ResponseEntity.created(location).build();*/
        return new ResponseEntity<>(new SingleResponseDto<>(createdMember), HttpStatus.CREATED);
    }

    //회원정보수정
    @PatchMapping("/info")
    public ResponseEntity<?> patchMember(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody MemberDto.Patch requestBody) {

        int memberId = jwtTokenizer.getMemberId(token);
        Member member = memberService.findMember(memberId);

        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }

        final Member updatedMember = member;
        Optional.ofNullable(requestBody.getName()).ifPresent(name -> updatedMember.setName(name));
        Optional.ofNullable(requestBody.getPassword()).ifPresent(password -> updatedMember.setPassword(password));

        member = memberService.updateMember(updatedMember);

        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)), HttpStatus.OK);
    }

    //회원 등록 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfoById(@RequestHeader("Authorization") String token) {
        int memberId = jwtTokenizer.getMemberId(token);
        Member member = memberService.findMember(memberId);
        if (member == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member not found");
        }
        return new ResponseEntity<>(new SingleResponseDto<>(mapper.memberToMemberResponse(member)), HttpStatus.OK);

    }

    @Transactional
    @PostMapping("addCart")
    public ResponseEntity addCartItem(@RequestHeader(value = "Authorization", required = false) String token,
                                        @RequestBody MemberDto.PostCart postCart) {

        memberService.addMyCart(jwtTokenizer.getMemberId(token), postCart.getProductId());

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    @GetMapping("getCart")
    public ResponseEntity getMyCart(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam int page,
                                    @RequestParam int size) {

        Pageable pageRequest = PageRequest.of(page, size);

        //List<Integer> productIds = memberService.getCartProductsId(jwtTokenizer.getMemberId(token));
        SingleResponseDto response = new SingleResponseDto(memberService.getCartProducts(jwtTokenizer.getMemberId(token), pageRequest));
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @GetMapping("find")
    public ResponseEntity getMemberInfo(@RequestHeader(value = "AUthorization", required = true) String token) {

        Member member = memberService.findMemberById(jwtTokenizer.getMemberId(token));

        MemberDto.Response response = new MemberDto.Response(member.getMemberId(), member.getEmail(), member.getName(), member.getPoint());

        return new ResponseEntity(response, HttpStatus.OK);
    }

}


