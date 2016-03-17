package com.adisoftwares.bookreader.epub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.adisoftwares.bookreader.BookData;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;

import java.util.List;

/**
 * Created by adityathanekar on 09/02/16.
 */
public class EpubBookData extends BookData {

    private Context context;
    private Package mPackage;
    private Container container;
    private String path;

    public EpubBookData(String path, Context context) throws Exception {
        super(path);
        this.context = context;
        this.path = path;
        container = EPub3.openBook(path);
        mPackage = container.getDefaultPackage();
        //thumbnail = new MuPDFThumb(context, path);
    }

    @Override
    public Bitmap getThumbnail(int width, int height) {
        return getCover();
    }

    private Bitmap getCover() {
        // Get the cover
        ManifestItem coverItm = null;
        List<ManifestItem> manifestItemList = mPackage.getManifestTable();
        coverItm = manifestItemList.get(0);
        if ( coverItm != null && coverItm.getMediaType().equals("image/jpeg") || coverItm.getMediaType().equals("image/png"))
        {
            Bitmap bitmap=null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                bitmap = BitmapFactory.decodeStream(mPackage.getInputStream(coverItm.getHref(), false), null, options);

            } catch (Exception e) {
                Log.d("Aditya", e.toString());
            }

            /*byte[] data = coverItm.getHref();

            FileOutputStream out;
            try {
                out = new FileOutputStream(context.getExternalCacheDir()+"/"+cache_dir+"/"+filename+extension);
                out.write(data);
                out.close();

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            return bitmap;
        }
        return null;
    }
}
