package com.example.test.service;


import com.example.test.entity.Actual;
import com.example.test.entity.Customer;
import com.example.test.entity.Price;
import com.example.test.entity.Product;
import com.example.test.mapper.ReflectionMapper;
import com.example.test.repository.ActualRepository;
import com.example.test.repository.CustomerRepository;
import com.example.test.repository.PriceRepository;
import com.example.test.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExcelParser {

    private final PriceRepository priceRepository;
    private final ActualRepository actualRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ExcelParser(PriceRepository priceRepository,
                       ActualRepository actualRepository,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository) {
        this.priceRepository = priceRepository;
        this.actualRepository = actualRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @PostConstruct
    private void initialize() {
        parseAndSave("src/main/resources/BackendDeveloper.xlsx");
    }

    @Transactional
    public void parseAndSave(String filePath) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            parseCustomers(workbook.getSheet("Customers"));
            parseProducts(workbook.getSheet("Products"));
            parsePrices(workbook.getSheet("Price"));
            parseActuals(workbook.getSheet("Actuals"));

            System.out.println("Успешно распарсено и сохранено в БД");

        } catch (IOException e) {
            System.err.println("Ошибка чтения Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parsePrices(Sheet sheet) {
        if (sheet == null) {
            System.err.println("Price не обнаружено");
            return;
        }

        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                Price price = new Price();

                String chainName = getCellValueAsString(row.getCell(0));
                price.setChainName(chainName);

                Integer materialNo = getCellValueAsInteger(row.getCell(1));
                Product product = productRepository.findByMaterialNo(materialNo);
                price.setProduct(product);

                BigDecimal regularPrice = getCellValueAsBigDecimal(row.getCell(2));
                price.setRegularPricePerUnit(regularPrice);

                priceRepository.save(price);

            } catch (Exception e) {
                System.err.println("Не получилось распарсить строку: " + row.getRowNum() + " - " + e.getMessage());
            }
        }
    }

    private void parseCustomers(Sheet sheet) {
        if (sheet == null) {
            System.err.println("Customers не обнаружено");
            return;
        }

        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                Customer customer = new Customer();

                Integer shipToCode = getCellValueAsInteger(row.getCell(0));
                customer.setCh3ShipToCode(shipToCode);

                String shipToName = getCellValueAsString(row.getCell(1));
                customer.setCh3ShipToName(shipToName);

                String chainName = getCellValueAsString(row.getCell(2));
                customer.setChainName(chainName);

                customerRepository.save(customer);

            } catch (Exception e) {
                System.err.println("Не получилось распарсить строку =: " + row.getRowNum() + " - " + e.getMessage());
            }
        }
    }

    private void parseProducts(Sheet sheet) {
        if (sheet == null) {
            System.err.println("Products не обнаружено");
            return;
        }

        Iterator<Row> rowIterator = sheet.iterator();

        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                Product product = new Product();

                Integer materialNo = getCellValueAsInteger(row.getCell(0));
                product.setMaterialNo(materialNo);

                String description = getCellValueAsString(row.getCell(1));
                product.setMaterialDescRus(description);

                Integer categoryCode = getCellValueAsInteger(row.getCell(2));
                product.setL3ProductCategoryCode(categoryCode);

                String categoryName = getCellValueAsString(row.getCell(3));
                product.setL3ProductCategoryName(categoryName);

                productRepository.save(product);

            } catch (Exception e) {
                System.err.println("Не получилось распарсить строку: " + row.getRowNum() + " - " + e.getMessage());
            }
        }
    }

    private void parseActuals(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            try {
                Actual actual = new Actual();

                Date date = getCellValueAsDate(row.getCell(0));
                actual.setDate(new java.sql.Date(date.getTime()));

                Integer materialNo = getCellValueAsInteger(row.getCell(1));

                Product product = productRepository.findByMaterialNo(materialNo);
                if (product == null) {
                    throw new RuntimeException("Product с materialNo " + materialNo + " не найден.");
                }
                actual.setProduct(product);

                Integer shipToCode = getCellValueAsInteger(row.getCell(2));

                Customer customer = customerRepository.findByCh3ShipToCode(shipToCode);
                if (customer == null) {
                    throw new RuntimeException("Customer с ch3ShipToCode " + shipToCode + " не найден.");
                }
                actual.setCustomer(customer);

                Integer volume = getCellValueAsInteger(row.getCell(3));
                actual.setVolumeUnits(volume);

                BigDecimal actualValue = getCellValueAsBigDecimal(row.getCell(4));
                actual.setActualSalesValue(actualValue);

                actualRepository.save(actual);

            } catch (Exception e) {
                System.err.println("Не получилось распарсить строку: " + row.getRowNum() + " - " + e.getMessage());
            }
        }

        System.out.println("Импорт Actuals завершен:");
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return dateFormat.format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                switch (cell.getCachedFormulaResultType()) {
                    case STRING:
                        return cell.getStringCellValue();
                    case NUMERIC:
                        return String.valueOf((long) cell.getNumericCellValue());
                    default:
                        return "";
                }
            default:
                return "";
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                try {
                    return new BigDecimal(cell.getStringCellValue().replace(",", "."));
                } catch (NumberFormatException e) {
                    return null;
                }
            case NUMERIC:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            case FORMULA:
                return BigDecimal.valueOf(cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case FORMULA:
                return (int) cell.getNumericCellValue();
            default:
                return null;
        }
    }

    private Date getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return dateFormat.parse(cell.getStringCellValue());
            } catch (ParseException e) {
                return null;
            }
        }

        return null;
    }
}
