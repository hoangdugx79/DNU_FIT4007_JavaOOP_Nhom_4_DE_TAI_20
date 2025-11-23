package service;

import domain.*;
import interfaces.Reportable;
import repository.*;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.BaseColor;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service tao bao cao va thong ke
 * CSV dep + PDF khong dau
 */
public class ReportService implements Reportable {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ReportService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public String generateInventoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        report.append("║                          BAO CAO TON KHO                                       ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════════════════╝\n");
        report.append("Ngay bao cao: ").append(LocalDate.now().format(DATE_FORMAT)).append("\n\n");

        java.util.List<Product> products = productRepository.findAll();

        Map<String, Long> countByType = products.stream()
                .collect(Collectors.groupingBy(Product::getProductType, Collectors.counting()));

        Map<String, Double> valueByType = products.stream()
                .collect(Collectors.groupingBy(
                        Product::getProductType,
                        Collectors.summingDouble(p -> p.getStockQuantity() * p.getImportPrice())
                ));

        report.append("1. THONG KE THEO LOAI SAN PHAM:\n");
        report.append(String.format("%-20s | %10s | %20s\n", "Loai", "So luong", "Gia tri ton kho"));
        report.append("-".repeat(60)).append("\n");

        double totalValue = 0;
        for (String type : countByType.keySet()) {
            long count = countByType.get(type);
            double value = valueByType.get(type);
            totalValue += value;
            report.append(String.format("%-20s | %,10d | %,20.0f VND\n", type, count, value));
        }
        report.append("-".repeat(60)).append("\n");
        report.append(String.format("TONG CONG: %,20.0f VND\n\n", totalValue));

        report.append("2. CHI TIET TON KHO:\n");
        report.append(String.format("%-10s | %-25s | %-12s | %10s | %15s | %15s\n",
                "ID", "Ten", "Loai", "Ton", "Gia nhap", "Gia ban"));
        report.append("-".repeat(100)).append("\n");

        for (Product product : products) {
            String name = product.getName().length() > 25 ?
                    product.getName().substring(0, 22) + "..." : product.getName();
            report.append(String.format("%-10s | %-25s | %-12s | %,10d | %,15.0f | %,15.0f\n",
                    product.getId(),
                    name,
                    product.getProductType(),
                    product.getStockQuantity(),
                    product.getImportPrice(),
                    product.getSalePrice()));
        }

        return report.toString();
    }

    @Override
    public String generateImportExportReport(LocalDate fromDate, LocalDate toDate) {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        report.append("║                       BAO CAO NHAP - XUAT - TON                                ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════════════════╝\n");
        report.append(String.format("Tu ngay: %s den %s\n\n",
                fromDate.format(DATE_FORMAT), toDate.format(DATE_FORMAT)));

        java.util.List<ImportOrder> imports = orderRepository.getImportOrdersByDateRange(fromDate, toDate);
        double totalImport = imports.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(ImportOrder::getTotalAmount)
                .sum();

        report.append("1. PHIEU NHAP KHO:\n");
        report.append(String.format("   Tong so phieu: %d\n", imports.size()));
        report.append(String.format("   Tong gia tri: %,20.0f VND\n\n", totalImport));

        java.util.List<ExportOrder> exports = orderRepository.getExportOrdersByDateRange(fromDate, toDate);
        double totalExport = exports.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .mapToDouble(ExportOrder::getTotalAmount)
                .sum();

        report.append("2. PHIEU XUAT KHO:\n");
        report.append(String.format("   Tong so phieu: %d\n", exports.size()));
        report.append(String.format("   Tong gia tri: %,20.0f VND\n\n", totalExport));

        java.util.List<Product> products = productRepository.findAll();
        double inventoryValue = products.stream()
                .mapToDouble(p -> p.getStockQuantity() * p.getImportPrice())
                .sum();

        report.append("3. TON KHO CUOI KY:\n");
        report.append(String.format("   So loai san pham: %d\n", products.size()));
        report.append(String.format("   Gia tri ton kho: %,20.0f VND\n\n", inventoryValue));

        return report.toString();
    }

    @Override
    public String generateRevenueReport(LocalDate fromDate, LocalDate toDate) {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        report.append("║                          BAO CAO DOANH THU                                     ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════════════════╝\n");
        report.append(String.format("Tu ngay: %s den %s\n\n",
                fromDate.format(DATE_FORMAT), toDate.format(DATE_FORMAT)));

        java.util.List<ExportOrder> exports = orderRepository.getExportOrdersByDateRange(fromDate, toDate)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        double totalRevenue = 0;
        double totalCost = 0;
        double totalProfit = 0;

        for (ExportOrder order : exports) {
            double revenue = order.getTotalAmount();
            double cost = 0;

            for (OrderItem item : order.getItems()) {
                cost += item.getProduct().getImportPrice() * item.getQuantity();
            }

            double profit = revenue - cost;

            totalRevenue += revenue;
            totalCost += cost;
            totalProfit += profit;
        }

        report.append(String.format("Tong doanh thu:       %,20.0f VND\n", totalRevenue));
        report.append(String.format("Tong chi phi:         %,20.0f VND\n", totalCost));
        report.append(String.format("Loi nhuan:            %,20.0f VND\n", totalProfit));
        report.append(String.format("Ty suat loi nhuan:    %18.2f %%\n",
                totalRevenue > 0 ? (totalProfit / totalRevenue * 100) : 0));

        report.append(String.format("\nSo don hang:          %,20d\n", exports.size()));
        report.append(String.format("Gia tri TB/don:       %,20.0f VND\n",
                exports.size() > 0 ? totalRevenue / exports.size() : 0));

        return report.toString();
    }

    public String generateSeasonalTrendReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        report.append("║                   BAO CAO XU HUONG BAN HANG THEO MUA                           ║\n");
        report.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n");

        java.util.List<ExportOrder> allExports = orderRepository.findAllExportOrders()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (allExports.isEmpty()) {
            report.append("Chua co du lieu ban hang!\n");
            return report.toString();
        }

        Map<String, Double> seasonRevenue = new LinkedHashMap<>();
        Map<String, Integer> seasonOrderCount = new LinkedHashMap<>();
        Map<String, Integer> seasonQuantity = new LinkedHashMap<>();

        seasonRevenue.put("Xuan (1-3)", 0.0);
        seasonRevenue.put("Ha (4-6)", 0.0);
        seasonRevenue.put("Thu (7-9)", 0.0);
        seasonRevenue.put("Dong (10-12)", 0.0);

        seasonOrderCount.put("Xuan (1-3)", 0);
        seasonOrderCount.put("Ha (4-6)", 0);
        seasonOrderCount.put("Thu (7-9)", 0);
        seasonOrderCount.put("Dong (10-12)", 0);

        seasonQuantity.put("Xuan (1-3)", 0);
        seasonQuantity.put("Ha (4-6)", 0);
        seasonQuantity.put("Thu (7-9)", 0);
        seasonQuantity.put("Dong (10-12)", 0);

        for (ExportOrder order : allExports) {
            int month = order.getOrderDate().getMonthValue();
            String season = getSeason(month);

            seasonRevenue.put(season, seasonRevenue.get(season) + order.getTotalAmount());
            seasonOrderCount.put(season, seasonOrderCount.get(season) + 1);

            int quantity = order.getItems().stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
            seasonQuantity.put(season, seasonQuantity.get(season) + quantity);
        }

        report.append("1. THONG KE DOANH THU THEO MUA:\n");
        report.append(String.format("%-15s | %15s | %15s | %15s | %20s\n",
                "Mua", "So don", "So luong", "Doanh thu", "TB/don"));
        report.append("-".repeat(90)).append("\n");

        double totalRevenue = 0;
        int totalOrders = 0;
        int totalQuantity = 0;

        for (String season : seasonRevenue.keySet()) {
            double revenue = seasonRevenue.get(season);
            int orders = seasonOrderCount.get(season);
            int quantity = seasonQuantity.get(season);
            double avgPerOrder = orders > 0 ? revenue / orders : 0;

            totalRevenue += revenue;
            totalOrders += orders;
            totalQuantity += quantity;

            report.append(String.format("%-15s | %,15d | %,15d | %,15.0f | %,20.0f\n",
                    season, orders, quantity, revenue, avgPerOrder));
        }

        report.append("-".repeat(90)).append("\n");
        report.append(String.format("TONG CONG      | %,15d | %,15d | %,15.0f | %,20.0f\n",
                totalOrders, totalQuantity, totalRevenue,
                totalOrders > 0 ? totalRevenue / totalOrders : 0));

        return report.toString();
    }

    private String getSeason(int month) {
        if (month >= 1 && month <= 3) return "Xuan (1-3)";
        if (month >= 4 && month <= 6) return "Ha (4-6)";
        if (month >= 7 && month <= 9) return "Thu (7-9)";
        return "Dong (10-12)";
    }

    @Override
    public void exportToCSV(String reportContent, String fileName) throws IOException {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String filePath = "reports/" + fileName;
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {
            writer.write('\ufeff'); // UTF-8 BOM
            writer.write(reportContent);
        }
        System.out.println("Xuat CSV: " + filePath);
    }

    /**
     * XUAT CSV - FORMAT BANG DEP, DE DOC
     */
    public void exportToExcelCSV(String reportType, LocalDate fromDate, LocalDate toDate) throws IOException {
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String fileName = reportType + "_" + LocalDate.now() + ".csv";
        String filePath = "reports/" + fileName;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"))) {

            writer.write('\ufeff'); // UTF-8 BOM

            if ("inventory".equals(reportType)) {
                // ========== BAO CAO TON KHO ==========
                writer.write("=== BAO CAO TON KHO ===\n");
                writer.write("Ngay xuat:," + LocalDate.now().format(DATE_FORMAT) + "\n");
                writer.write("\n");

                // HEADER
                writer.write("ID SAN PHAM,TEN SAN PHAM,LOAI,TON KHO,GIA NHAP (VND),GIA BAN (VND),GIA TRI TON (VND)\n");
                writer.write("---,---,---,---,---,---,---\n");

                // DATA
                java.util.List<Product> products = productRepository.findAll();
                double totalValue = 0;

                for (Product product : products) {
                    double value = product.getStockQuantity() * product.getImportPrice();
                    totalValue += value;

                    writer.write(String.format("%s,\"%s\",%s,%d,%.0f,%.0f,%.0f\n",
                            product.getId(),
                            product.getName(),
                            product.getProductType(),
                            product.getStockQuantity(),
                            product.getImportPrice(),
                            product.getSalePrice(),
                            value));
                }

                // TOTAL
                writer.write("---,---,---,---,---,---,---\n");
                writer.write(String.format(",,,,,TONG CONG:,%.0f\n", totalValue));

                System.out.println("So san pham: " + products.size());
                System.out.println("Tong gia tri: " + totalValue);

            } else if ("revenue".equals(reportType)) {
                // ========== BAO CAO DOANH THU ==========
                writer.write("=== BAO CAO DOANH THU ===\n");
                writer.write("Tu ngay:," + (fromDate != null ? fromDate.format(DATE_FORMAT) : "N/A") + "\n");
                writer.write("Den ngay:," + (toDate != null ? toDate.format(DATE_FORMAT) : "N/A") + "\n");
                writer.write("Ngay xuat:," + LocalDate.now().format(DATE_FORMAT) + "\n");
                writer.write("\n");

                // HEADER
                writer.write("ID DON,NGAY,KHACH HANG,TONG TIEN (VND),CHI PHI (VND),LOI NHUAN (VND)\n");
                writer.write("---,---,---,---,---,---\n");

                // DATA
                java.util.List<ExportOrder> exports = orderRepository.getExportOrdersByDateRange(
                                fromDate != null ? fromDate : LocalDate.now().minusMonths(1),
                                toDate != null ? toDate : LocalDate.now())
                        .stream()
                        .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                        .collect(Collectors.toList());

                double totalRevenue = 0;
                double totalCost = 0;
                double totalProfit = 0;

                for (ExportOrder order : exports) {
                    double revenue = order.getTotalAmount();
                    double cost = 0;

                    for (OrderItem item : order.getItems()) {
                        cost += item.getProduct().getImportPrice() * item.getQuantity();
                    }
                    double profit = revenue - cost;

                    totalRevenue += revenue;
                    totalCost += cost;
                    totalProfit += profit;

                    String custName = order.getCustomer() != null ? order.getCustomer().getName() : "N/A";

                    writer.write(String.format("%s,%s,\"%s\",%.0f,%.0f,%.0f\n",
                            order.getId(),
                            order.getOrderDate().format(DATE_FORMAT),
                            custName,
                            revenue,
                            cost,
                            profit));
                }

                // TOTAL
                writer.write("---,---,---,---,---,---\n");
                writer.write(String.format(",,,TONG CONG:,%.0f,%.0f,%.0f\n", totalRevenue, totalCost, totalProfit));

                // SUMMARY
                writer.write("\n");
                writer.write("=== THONG KE TONG HOP ===\n");
                writer.write("Chi tieu,Gia tri\n");
                writer.write(String.format("Tong doanh thu,%.0f VND\n", totalRevenue));
                writer.write(String.format("Tong chi phi,%.0f VND\n", totalCost));
                writer.write(String.format("Loi nhuan,%.0f VND\n", totalProfit));
                writer.write(String.format("Ty suat loi nhuan,%.2f %%\n",
                        totalRevenue > 0 ? (totalProfit / totalRevenue * 100) : 0));
                writer.write(String.format("So don hang,%d\n", exports.size()));
                writer.write(String.format("Gia tri TB/don,%.0f VND\n",
                        exports.size() > 0 ? totalRevenue / exports.size() : 0));

                System.out.println("So don hang: " + exports.size());
                System.out.println("Tong doanh thu: " + totalRevenue);
            }
        }

        System.out.println("Xuat CSV Excel: " + filePath);
    }

    /**
     * XUAT PDF - KHONG DAU, CO BANG
     */
    public void exportToPDF(String reportType, String fileName) throws IOException, DocumentException {
        if (reportType == null || reportType.trim().isEmpty()) {
            System.out.println("LOI: reportType rong!");
            return;
        }

        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }

        String filePath = "reports/" + fileName;
        String typeNormalized = reportType.trim().toLowerCase();

        System.out.println("\n=== XUAT PDF ===");
        System.out.println("reportType: " + reportType);
        System.out.println("filePath: " + filePath);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph title = new Paragraph("HE THONG QUAN LY KHO XUAT NHAP HANG\n", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph date = new Paragraph("Ngay xuat: " + LocalDate.now().format(DATE_FORMAT) + "\n\n", dateFont);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);

        try {
            if (typeNormalized.contains("inventory")) {
                System.out.println("-> Them bang TON KHO");
                addInventoryTable(document);
            } else if (typeNormalized.contains("revenue")) {
                System.out.println("-> Them bang DOANH THU");
                addRevenueTable(document);
            } else if (typeNormalized.contains("seasonal")) {
                System.out.println("-> Them bang XU HUONG MUA");
                addSeasonalTable(document);
            } else {
                System.out.println("ERROR: reportType khong hop le");
                Font errorFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
                document.add(new Paragraph("LOI: Khong biet loai bao cao: " + reportType, errorFont));
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

        document.close();
        System.out.println("PDF da xuat: " + filePath);
        System.out.println("=== KET THUC ===\n");
    }

    private void addInventoryTable(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Paragraph subtitle = new Paragraph("BAO CAO TON KHO\n\n", titleFont);
        document.add(subtitle);

        java.util.List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            Font warnFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            document.add(new Paragraph("KHONG CO SAN PHAM NAO!", warnFont));
            return;
        }

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2.5f, 1.5f, 1, 1.5f, 1.5f, 1.8f});

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        String[] headers = {"ID", "Ten San Pham", "Loai", "Ton Kho", "Gia Nhap", "Gia Ban", "Gia Tri Ton"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
        double totalInventoryValue = 0;

        for (Product product : products) {
            double value = product.getStockQuantity() * product.getImportPrice();
            totalInventoryValue += value;

            String productName = convertToNonDiacritic(product.getName());
            String productType = convertToNonDiacritic(product.getProductType());

            table.addCell(new PdfPCell(new Paragraph(product.getId(), dataFont)));
            table.addCell(new PdfPCell(new Paragraph(productName, dataFont)));
            table.addCell(new PdfPCell(new Paragraph(productType, dataFont)));

            PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(product.getStockQuantity()), dataFont));
            qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(qtyCell);

            PdfPCell importCell = new PdfPCell(new Paragraph(String.format("%.0f", product.getImportPrice()), dataFont));
            importCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(importCell);

            PdfPCell saleCell = new PdfPCell(new Paragraph(String.format("%.0f", product.getSalePrice()), dataFont));
            saleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(saleCell);

            PdfPCell valueCell = new PdfPCell(new Paragraph(String.format("%.0f", value), dataFont));
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(valueCell);
        }

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell totalLabel = new PdfPCell(new Paragraph("TONG CONG", totalFont));
        totalLabel.setColspan(6);
        totalLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(4);
        table.addCell(totalLabel);

        PdfPCell totalValueCell = new PdfPCell(new Paragraph(String.format("%.0f", totalInventoryValue), totalFont));
        totalValueCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPadding(4);
        table.addCell(totalValueCell);

        document.add(table);
    }

    private void addRevenueTable(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Paragraph subtitle = new Paragraph("BAO CAO DOANH THU\n\n", titleFont);
        document.add(subtitle);

        LocalDate fromDate = LocalDate.now().minusMonths(1);
        LocalDate toDate = LocalDate.now();

        java.util.List<ExportOrder> exports = orderRepository.getExportOrdersByDateRange(fromDate, toDate)
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (exports.isEmpty()) {
            Font warnFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            document.add(new Paragraph("KHONG CO DON HANG!", warnFont));
            return;
        }

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        String[] headers = {"ID Don", "Ngay", "Khach Hang", "Tong Tien", "Chi Phi", "Loi Nhuan"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
        double totalRev = 0, totalCost = 0, totalProfit = 0;

        for (ExportOrder order : exports) {
            double revenue = order.getTotalAmount();
            double cost = 0;
            for (OrderItem item : order.getItems()) {
                cost += item.getProduct().getImportPrice() * item.getQuantity();
            }
            double profit = revenue - cost;

            totalRev += revenue;
            totalCost += cost;
            totalProfit += profit;

            String custName = order.getCustomer() != null
                    ? convertToNonDiacritic(order.getCustomer().getName())
                    : "N/A";

            table.addCell(new PdfPCell(new Paragraph(order.getId(), dataFont)));
            table.addCell(new PdfPCell(new Paragraph(order.getOrderDate().format(DATE_FORMAT), dataFont)));
            table.addCell(new PdfPCell(new Paragraph(custName, dataFont)));

            PdfPCell revCell = new PdfPCell(new Paragraph(String.format("%.0f", revenue), dataFont));
            revCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(revCell);

            PdfPCell costCell = new PdfPCell(new Paragraph(String.format("%.0f", cost), dataFont));
            costCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(costCell);

            PdfPCell profitCell = new PdfPCell(new Paragraph(String.format("%.0f", profit), dataFont));
            profitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(profitCell);
        }

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell totalLabel = new PdfPCell(new Paragraph("TONG CONG", totalFont));
        totalLabel.setColspan(3);
        totalLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(4);
        table.addCell(totalLabel);

        PdfPCell totalRevCell = new PdfPCell(new Paragraph(String.format("%.0f", totalRev), totalFont));
        totalRevCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalRevCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalRevCell.setPadding(4);
        table.addCell(totalRevCell);

        PdfPCell totalCostCell = new PdfPCell(new Paragraph(String.format("%.0f", totalCost), totalFont));
        totalCostCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalCostCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCostCell.setPadding(4);
        table.addCell(totalCostCell);

        PdfPCell totalProfitCell = new PdfPCell(new Paragraph(String.format("%.0f", totalProfit), totalFont));
        totalProfitCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalProfitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalProfitCell.setPadding(4);
        table.addCell(totalProfitCell);

        document.add(table);
    }

    private void addSeasonalTable(Document document) throws DocumentException {
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Paragraph subtitle = new Paragraph("BAO CAO XU HUONG BAN HANG THEO MUA\n\n", titleFont);
        document.add(subtitle);

        java.util.List<ExportOrder> completedExports = orderRepository.findAllExportOrders()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedExports.isEmpty()) {
            Font warnFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            document.add(new Paragraph("KHONG CO DU LIEU BAN HANG!", warnFont));
            return;
        }

        Map<String, Double> seasonRevenue = new LinkedHashMap<>();
        Map<String, Integer> seasonOrders = new LinkedHashMap<>();
        Map<String, Integer> seasonQty = new LinkedHashMap<>();

        seasonRevenue.put("Xuan (1-3)", 0.0);
        seasonRevenue.put("Ha (4-6)", 0.0);
        seasonRevenue.put("Thu (7-9)", 0.0);
        seasonRevenue.put("Dong (10-12)", 0.0);
        seasonOrders.put("Xuan (1-3)", 0);
        seasonOrders.put("Ha (4-6)", 0);
        seasonOrders.put("Thu (7-9)", 0);
        seasonOrders.put("Dong (10-12)", 0);
        seasonQty.put("Xuan (1-3)", 0);
        seasonQty.put("Ha (4-6)", 0);
        seasonQty.put("Thu (7-9)", 0);
        seasonQty.put("Dong (10-12)", 0);

        for (ExportOrder order : completedExports) {
            int month = order.getOrderDate().getMonthValue();
            String season = getSeason(month);

            seasonRevenue.put(season, seasonRevenue.get(season) + order.getTotalAmount());
            seasonOrders.put(season, seasonOrders.get(season) + 1);

            int qty = order.getItems().stream().mapToInt(OrderItem::getQuantity).sum();
            seasonQty.put(season, seasonQty.get(season) + qty);
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        String[] headers = {"Mua", "So Don", "So Luong", "Doanh Thu", "Trung Binh/Don"};

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }

        Font dataFont = new Font(Font.FontFamily.HELVETICA, 8);
        double totalRev = 0;
        int totalOrders = 0, totalQty = 0;

        for (String season : seasonRevenue.keySet()) {
            double rev = seasonRevenue.get(season);
            int ord = seasonOrders.get(season);
            int qty = seasonQty.get(season);
            double avg = ord > 0 ? rev / ord : 0;

            totalRev += rev;
            totalOrders += ord;
            totalQty += qty;

            table.addCell(new PdfPCell(new Paragraph(season, dataFont)));

            PdfPCell ordCell = new PdfPCell(new Paragraph(String.valueOf(ord), dataFont));
            ordCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(ordCell);

            PdfPCell qtyCell = new PdfPCell(new Paragraph(String.valueOf(qty), dataFont));
            qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(qtyCell);

            PdfPCell revCell = new PdfPCell(new Paragraph(String.format("%.0f", rev), dataFont));
            revCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(revCell);

            PdfPCell avgCell = new PdfPCell(new Paragraph(String.format("%.0f", avg), dataFont));
            avgCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(avgCell);
        }

        Font totalFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell totalLabel = new PdfPCell(new Paragraph("TONG CONG", totalFont));
        totalLabel.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalLabel.setPadding(4);
        table.addCell(totalLabel);

        PdfPCell totalOrdCell = new PdfPCell(new Paragraph(String.valueOf(totalOrders), totalFont));
        totalOrdCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalOrdCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalOrdCell.setPadding(4);
        table.addCell(totalOrdCell);

        PdfPCell totalQtyCell = new PdfPCell(new Paragraph(String.valueOf(totalQty), totalFont));
        totalQtyCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalQtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalQtyCell.setPadding(4);
        table.addCell(totalQtyCell);

        PdfPCell totalRevCell = new PdfPCell(new Paragraph(String.format("%.0f", totalRev), totalFont));
        totalRevCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalRevCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalRevCell.setPadding(4);
        table.addCell(totalRevCell);

        PdfPCell totalAvgCell = new PdfPCell(new Paragraph(
                String.format("%.0f", totalOrders > 0 ? totalRev / totalOrders : 0), totalFont));
        totalAvgCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        totalAvgCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalAvgCell.setPadding(4);
        table.addCell(totalAvgCell);

        document.add(table);
    }

    /**
     * CONVERT DIACRITIC SANG NON-DIACRITIC
     */
    private String convertToNonDiacritic(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[][] table = {
                {"À", "A"}, {"Á", "A"}, {"Ả", "A"}, {"Ã", "A"}, {"Ạ", "A"},
                {"Ă", "A"}, {"Ằ", "A"}, {"Ắ", "A"}, {"Ẳ", "A"}, {"Ẵ", "A"}, {"Ặ", "A"},
                {"à", "a"}, {"á", "a"}, {"ả", "a"}, {"ã", "a"}, {"ạ", "a"},
                {"ă", "a"}, {"ằ", "a"}, {"ắ", "a"}, {"ẳ", "a"}, {"ẵ", "a"}, {"ặ", "a"},
                {"È", "E"}, {"É", "E"}, {"Ẻ", "E"}, {"Ẽ", "E"}, {"Ẹ", "E"},
                {"Ê", "E"}, {"Ề", "E"}, {"Ế", "E"}, {"Ể", "E"}, {"Ễ", "E"}, {"Ệ", "E"},
                {"è", "e"}, {"é", "e"}, {"ẻ", "e"}, {"ẽ", "e"}, {"ẹ", "e"},
                {"ê", "e"}, {"ề", "e"}, {"ế", "e"}, {"ể", "e"}, {"ễ", "e"}, {"ệ", "e"},
                {"Ì", "I"}, {"Í", "I"}, {"Ỉ", "I"}, {"Ĩ", "I"}, {"Ị", "I"},
                {"ì", "i"}, {"í", "i"}, {"ỉ", "i"}, {"ĩ", "i"}, {"ị", "i"},
                {"Ò", "O"}, {"Ó", "O"}, {"Ỏ", "O"}, {"Õ", "O"}, {"Ọ", "O"},
                {"Ô", "O"}, {"Ồ", "O"}, {"Ố", "O"}, {"Ổ", "O"}, {"Ỗ", "O"}, {"Ộ", "O"},
                {"Ơ", "O"}, {"Ờ", "O"}, {"Ớ", "O"}, {"Ở", "O"}, {"Ỡ", "O"}, {"Ợ", "O"},
                {"ò", "o"}, {"ó", "o"}, {"ỏ", "o"}, {"õ", "o"}, {"ọ", "o"},
                {"ô", "o"}, {"ồ", "o"}, {"ố", "o"}, {"ổ", "o"}, {"ỗ", "o"}, {"ộ", "o"},
                {"ơ", "o"}, {"ờ", "o"}, {"ớ", "o"}, {"ở", "o"}, {"ỡ", "o"}, {"ợ", "o"},
                {"Ù", "U"}, {"Ú", "U"}, {"Ủ", "U"}, {"Ũ", "U"}, {"Ụ", "U"},
                {"Ư", "U"}, {"Ừ", "U"}, {"Ứ", "U"}, {"Ử", "U"}, {"Ữ", "U"}, {"Ự", "U"},
                {"ù", "u"}, {"ú", "u"}, {"ủ", "u"}, {"ũ", "u"}, {"ụ", "u"},
                {"ư", "u"}, {"ừ", "u"}, {"ứ", "u"}, {"ử", "u"}, {"ữ", "u"}, {"ự", "u"},
                {"Ỳ", "Y"}, {"Ý", "Y"}, {"Ỷ", "Y"}, {"Ỹ", "Y"}, {"Ỵ", "Y"},
                {"ỳ", "y"}, {"ý", "y"}, {"ỷ", "y"}, {"ỹ", "y"}, {"ỵ", "y"},
                {"Đ", "D"}, {"đ", "d"}
        };

        String result = text;
        for (String[] pair : table) {
            result = result.replace(pair[0], pair[1]);
        }

        return result;
    }

    @Override
    public java.util.List<String> getTopSellingProducts(int topN) {
        Map<String, Integer> salesCount = new HashMap<>();
        Map<String, String> productNames = new HashMap<>();

        java.util.List<ExportOrder> exports = orderRepository.findAllExportOrders()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        for (ExportOrder order : exports) {
            for (OrderItem item : order.getItems()) {
                String productId = item.getProduct().getId();
                String productName = item.getProduct().getName();

                salesCount.put(productId,
                        salesCount.getOrDefault(productId, 0) + item.getQuantity());
                productNames.put(productId, productName);
            }
        }

        return salesCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(entry -> String.format("%s - %s: %,d san pham",
                        entry.getKey(),
                        productNames.get(entry.getKey()),
                        entry.getValue()))
                .collect(Collectors.toList());
    }

    public String generateTopSellingReport(int topN) {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════════════════════╗\n");
        report.append(String.format("║                    TOP %d SAN PHAM BAN CHAY NHAT                              ║\n", topN));
        report.append("╚════════════════════════════════════════════════════════════════════════════════╝\n\n");

        java.util.List<String> topProducts = getTopSellingProducts(topN);

        int rank = 1;
        for (String product : topProducts) {
            report.append(String.format("%d. %s\n", rank++, product));
        }

        return report.toString();
    }
}

