//package greenNare.product.data;
//
//import greenNare.product.entity.Image;
//import greenNare.product.entity.Product;
//import greenNare.product.repository.ImageRepository;
//import greenNare.product.repository.ProductRepository;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.springframework.stereotype.Service;
//
//import javax.persistence.Column;
//import javax.transaction.Transactional;
//import java.awt.*;
//import java.io.FileInputStream;
//import java.io.IOException;
//
//@Service
//public class ExcelDataLoader {
//    ProductRepository productRepository;
//    ImageRepository imageRepository;
//
//    public ExcelDataLoader(ProductRepository productRepository, ImageRepository imageRepository){
//        this.productRepository = productRepository;
//        this.imageRepository = imageRepository;
//    }
//
//    @Transactional
//    public void loadExcelData(String filePath) throws IOException {
//        FileInputStream file = new FileInputStream(filePath);
//        Workbook workbook = WorkbookFactory.create(file);
//        //첫번째 시트 사용
//        Sheet sheet = workbook.getSheetAt(0);
//
//        for(Row row : sheet) {
//            //첫번째 행 생략
//            if(row.getRowNum() == 0) {
//                continue;
//            }
//
//            String productName = row.getCell(3).getStringCellValue();
//            String detail = row.getCell(4).getStringCellValue();
//            int price = (int)row.getCell(2).getNumericCellValue();
//            int point = price/10;
//            String storeLink = row.getCell(0).getStringCellValue();
//            String category = row.getCell(5).getStringCellValue();
//
//            Product product = new Product();
//            product.setProductName(productName);
//            product.setDetail(detail);
//            product.setPrice(price);
//            product.setPoint(point);
//            product.setStoreLink(storeLink);
//            product.setCategory(category);
//
//            productRepository.save(product);
//
//            String imageUri = row.getCell(1).getStringCellValue();
//
//            Image image = new Image();
//            image.setImageUri(imageUri);
//            image.setProduct(product);
//
//            imageRepository.save(image);
//
//        }
//
//        workbook.close();
//        file.close();
//
//    }
//
//
//}
