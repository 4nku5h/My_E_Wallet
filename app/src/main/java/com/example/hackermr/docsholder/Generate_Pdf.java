package com.example.hackermr.docsholder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.shrivastava.myewallet.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Generate_Pdf {

    public void generate(Context context,String projectname,ArrayList<Uri> listofUri,String text,String outPath) throws IOException {
        Document doc =new Document(PageSize.A4,2,2,2,2);
        //String outPath= Environment.getExternalStorageDirectory().toString();
        File app_folder = new File(outPath);         //creating main dir
        if(!app_folder.exists()){
            boolean success=app_folder.mkdir();
            if(success==false){
                Toast.makeText(context,"Somethng Went Wrong",Toast.LENGTH_SHORT).show();
            }

        }
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(outPath + "/" + projectname + ".pdf"));

            doc.open();
            if(text!=null){             // runs when user wants to generate pdf from text
                doc.add(new Paragraph(text));
                Drawable d = context.getResources().getDrawable(R.drawable.pdf_end_banner);
                BitmapDrawable bitDw = ((BitmapDrawable) d);
                Bitmap bmp = bitDw.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image image = Image.getInstance(stream.toByteArray());
                image.setCompressionLevel(9);
                image.scaleToFit(PageSize.A4.getWidth(), image.getHeight());
                doc.add(image);
                doc.close();
            }
            Toast.makeText(context,"Pdf Generated "+outPath,Toast.LENGTH_SHORT).show();

        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
}
