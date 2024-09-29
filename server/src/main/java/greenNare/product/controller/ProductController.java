package greenNare.product.controller;


import greenNare.Response.MultiResponseDto;
import greenNare.Response.SingleResponseDto;
import greenNare.auth.jwt.JwtTokenizer;
import greenNare.cart.service.CartService;
import greenNare.member.service.MemberService;
import greenNare.product.dto.GetProductWithImageDto;
import greenNare.product.entity.Product;
import greenNare.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
//@CrossOrigin(origins = "*")
@RequestMapping("/green")
public class ProductController {
    private ProductService productService;
    private JwtTokenizer jwtTokenizer;
    private MemberService memberService;

    public ProductController(ProductService productService, JwtTokenizer jwtTokenizer, MemberService memberService) {
        this.productService = productService;
        this.jwtTokenizer = jwtTokenizer;
        this.memberService = memberService;
    }


    //카테고리별 상품 조회
    @GetMapping
    public ResponseEntity getProducts(@RequestParam("page") int page,
                                      @RequestParam("size") int size,
                                      @RequestParam("category") String category,
                                      @RequestHeader(value = "Authorization", required = false) String token){

        Page<Product> getProducts = productService.getProducts(page, size, category);

        if(token == null) {
            // ProductService.class_existInMyCart() 매번 호출하지 않아도됨(로그인하지 않아 카트상품 없으므로 매번 호출하는것은 불필요)
            List<GetProductWithImageDto> responseProducts = productService.getProductsWithImage(getProducts, false);
            MultiResponseDto response = new MultiResponseDto(responseProducts, getProducts);
            return new ResponseEntity<>(response, HttpStatus.OK);

            //한가지 service 메서드로 사용가능
            // List<GetProductWithImageDto>에 넣을 객체마다 매번 ProductService.class_existInMyCart() 호출하게되는데 불필요하다고 예상
            // -> ProductService.class_getProductsWithImage() 에서 빈 리스트를 구분하여 ProductService.class_existInMyCart()를 매번 호출하지 않도록함
//            List<Integer> cartProductId = new ArrayList<>();
//            List<GetProductWithImageDto> responseProductsWithCart = productService.getProductsWithImage(getProducts, cartProductId);
//            MultiResponseDto response = new MultiResponseDto(responseProductsWithCart, getProducts);
//            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        //토큰있으면 카트상품리스트도 같이 전송
        else{
            List<Integer> cartProductId = memberService.getCartProductsId(jwtTokenizer.getMemberId(token));
            List<GetProductWithImageDto> responseProductsWithCart = productService.getProductsWithImage(getProducts, cartProductId);
            MultiResponseDto response = new MultiResponseDto(responseProductsWithCart, getProducts);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

    }


    //상품 상세정보 조회
    @GetMapping("/{productId}")
    public ResponseEntity getProduct(@PathVariable("productId") int productId) {
        GetProductWithImageDto productDetails = productService.getProductWithImage(productId);

        SingleResponseDto response = new SingleResponseDto(productDetails);

        return new ResponseEntity(response, HttpStatus.OK);
    }


    //상품 검색
    @GetMapping("/search")
    public ResponseEntity findProduct(@RequestParam("productName") String productName,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size) {

        List<Product> Products = productService.findProducts(productName);
        SingleResponseDto response = new SingleResponseDto(Products);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
