package in.mobiux.android.orca50scanner.util.pdf;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import in.mobiux.android.orca50scanner.api.model.Inventory;
import in.mobiux.android.orca50scanner.util.AppLogger;
import in.mobiux.android.orca50scanner.util.AppUtils;
import in.mobiux.android.orca50scanner.util.Common;

public class PdfUtils {

    private static final String TAG = PdfUtils.class.getCanonicalName();
    private Context context;
    private AppLogger logger;

    private static final String PDF_FILE_NAME = "footprints.pdf";
    private static final String PRINT_JOB_NAME = "footprints documents";
    private static final String PDF_AUTHOR = "Footprints";
    private static final String PDF_CREATER = "SGUL";
    public static final String PDF_WATERMARK_TEXT = "Footprints.com";


    public PdfUtils(Context context) {
        this.context = context;
        logger = AppLogger.getInstance(context);
    }

    //    path ~ "file_name.pdf"
    public void createPdfFile(String path, List<Inventory> list, String title) {

        if (new File(context.getFilesDir(), path).exists()) {
            new File(context.getFilesDir(), path).delete();
        }

        try {

            Document document = new Document();
//            save
//            FileOutputStream out = context.openFileOutput(path, Context.MODE_PRIVATE);
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(path));
//            Adding watermark in pdf
            pdfWriter.setPageEvent(new WatermarkPageEvent());
//            open to write
            document.open();

//            settings
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor(PDF_AUTHOR);
            document.addCreator(PDF_CREATER);


//            Adding header
            addHeaderTitle(document, "SGUL");
            addLineSpace(document);

            addNewItem(document, title, Element.ALIGN_CENTER);
            addNewItem(document, "created At : " + AppUtils.getFormattedTimestampUpToSeconds(), Element.ALIGN_RIGHT);

            addLineSeparator(document);
            addLineSpace(document);


            PdfPTable table = new PdfPTable(4);

            table.addCell("Asset Name");
            table.addCell("Barcode");
            table.addCell("RFID");
            table.addCell("Status");

            for (Inventory i : list) {

                table.addCell(i.getName());
                table.addCell(i.getBarcode());
                table.addCell(i.getEpc());
                if (i.isScanStatus()) {
                    table.addCell("Scanned");
                } else {
                    table.addCell("n/a");
                }

            }

            document.add(table);

            document.close();
            printPDF();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    //    path ~ "file_name.pdf"
    public void createPdfFile(String path) {

        if (new File(context.getFilesDir(), path).exists()) {
            new File(context.getFilesDir(), path).delete();
        }
        logger.i(TAG, "" + path);

        try {

            Document document = new Document();
//            save
//            FileOutputStream out = context.openFileOutput(path, Context.MODE_PRIVATE);
            PdfWriter.getInstance(document, new FileOutputStream(path));
//            open to write
            document.open();

            logger.i(TAG, "document is open for write");

//            settings
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor(PDF_AUTHOR);
            document.addCreator(PDF_CREATER);


            addNewItem(document, "some text here", Element.ALIGN_CENTER);

            addLineSeparator(document);

            for (int i = 0; i < 250; i++) {
                addNewItem(document, i + " some text here " + i, Element.ALIGN_LEFT);
            }

            document.close();
            printPDF();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void printPDF() {

        try {
            PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);

            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(context, getPdfPath(context));
            printManager.print(PRINT_JOB_NAME, printDocumentAdapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addHeaderTitle(Document document, String text) {

        Font blue = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.ORANGE);

        Chunk chunk = new Chunk(text, blue);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(Element.ALIGN_CENTER);

        try {
            document.add(paragraph);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void addNewItem(Document document, String text, int align) {
        Chunk chunk = new Chunk(text);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setAlignment(align);
        try {
            document.add(paragraph);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void addLineSeparator(Document document) throws DocumentException {
        LineSeparator lineSeparator = new LineSeparator();
        lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

        addLineSpace(document);
        document.add(new Chunk(lineSeparator));
        addLineSpace(document);
    }

    private void addLineSpace(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
    }

    public static final String getAppPath(Context context) {
        return Common.getAppPath(context);
    }

    public static final String getPdfPath(Context context) {
        return getAppPath(context) + PDF_FILE_NAME;
    }

    public static final String getPdfFileName() {
        return PDF_FILE_NAME;
    }
}
