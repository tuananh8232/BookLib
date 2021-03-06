package luubieunghi.lbn.booklib.UI.ReadBook.new_UI;/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.Config;
import com.folioreader.FolioReader;
import com.folioreader.model.HighLight;
import com.folioreader.model.locators.ReadLocator;
import com.folioreader.ui.base.OnSaveHighlight;
import com.folioreader.util.AppUtil;
import com.folioreader.util.OnHighlightListener;
import com.folioreader.util.ReadLocatorListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import luubieunghi.lbn.booklib.Database.BookDatabase;
import luubieunghi.lbn.booklib.Model.Book.Book;
import luubieunghi.lbn.booklib.Model.BookFile.BookFile;
import luubieunghi.lbn.booklib.R;
import luubieunghi.lbn.booklib.UI.CustomAlertDialog.BookLoadingAlertDialog;
import luubieunghi.lbn.booklib.UI.ReadBook.HighlightData.HighlightData;
import luubieunghi.lbn.booklib.Utility.Others.AppExecutors;

public class BookReadingActivity extends AppCompatActivity
        implements OnHighlightListener, ReadLocatorListener, FolioReader.OnClosedListener {

    private static final String LOG_TAG = BookReadingActivity.class.getSimpleName();
    private FolioReader folioReader;

    List<BookFile> listBookFile;
    BookLoadingAlertDialog dialog;
    String link = "";
    Book book;
    long book_ID;
    ReadLocator _readLocator;
    String filePath;
    String mBFileID;
    String mBFileTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reading);
        dialog = new BookLoadingAlertDialog(this);

        receiveData();
    }

    private void receiveData() {
        Intent intent = getIntent();
        book = (Book) intent.getSerializableExtra("book");
        book_ID = book.getBookID();

        dialog.showDialog();

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                listBookFile = BookDatabase.getInstance(BookReadingActivity.this).BookFileDAO().getAllFilesOfBook(book_ID);
                if (listBookFile.size() <= 0)
                    return;
                filePath = listBookFile.get(0).getBFilePath();

                if (listBookFile.get(0).getBLocator() != null) {
                    _readLocator = ReadLocator.fromJson(listBookFile.get(0).getBLocator());
                    Log.e(LOG_TAG + "_SAVE", " -> getReadLocator -> " + _readLocator.toJson());
                }

                dialog.hideDialog();
//                Log.e(LOG_TAG + "_SAVE", " -> getReadLocator -> " + mreadLocator.toString());
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        setUpView();
                    }
                });
            }
        });
    }

    private void setUpView() {

        folioReader = FolioReader.get()
                .setOnHighlightListener(this)
                .setReadLocatorListener(this)
                .setOnClosedListener(this);

        getHighlightsAndSave();

        Config config = AppUtil.getSavedConfig(getApplicationContext());
        if (config == null)
            config = new Config();
        config.setAllowedDirection(Config.AllowedDirection.VERTICAL_AND_HORIZONTAL);
        if (_readLocator != null) {
            folioReader.setReadLocator(_readLocator);
            Log.i(LOG_TAG + "_SAVE", "-> getReadLocator -> " + listBookFile.get(0).getBLocator());
        }
        folioReader.setConfig(config, true)
                .openBook(filePath);
    }

    @Override
    public void saveReadLocator(ReadLocator readLocator) {
        listBookFile.get(0).setbLocator(readLocator.toJson());
        mBFileID = getBFileID(readLocator);
        mBFileTitle = getTitleFileID(readLocator);
        listBookFile.get(0).setBFileID(mBFileID);
        listBookFile.get(0).setBRead(1);

        // listBookFile.get(0).setBRead(sotrang);
        ReadLocator rl = ReadLocator.fromJson(listBookFile.get(0).getBLocator());
        Log.i(LOG_TAG + "_SAVE", "-> saveReadLocator -> " + listBookFile.get(0).getBLocator());
        Log.i(LOG_TAG + "_SAVE"," -> saveBfileID -> "+ getBFileID(readLocator));
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                BookDatabase.getInstance(BookReadingActivity.this).BookFileDAO().updateBookFile(listBookFile.get(0));
            }
        });
    }

    private String getTitleFileID(ReadLocator readLocator) {
        return "";
    }

    private String getBFileID(ReadLocator readLocator) {
        String[] tmp_parser_1 = readLocator.toJson().split(",");
        String[] tmp_parser_2 = tmp_parser_1[0].split(":");
        String _bFileID = tmp_parser_2[1].substring(1,tmp_parser_2[1].length()-1);
        return _bFileID;
    }

    /*
     * For testing purpose, we are getting dummy highlights from asset. But you can get highlights from your server
     * On success, you can save highlights to FolioReader DB.
     */
    private void getHighlightsAndSave() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<HighLight> highlightList = null;
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    highlightList = objectMapper.readValue(
                            loadAssetTextAsString("highlights/highlights_data.json"),
                            new TypeReference<List<HighlightData>>() {
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (highlightList == null) {
                    folioReader.saveReceivedHighLights(highlightList, new OnSaveHighlight() {
                        @Override
                        public void onFinished() {
                            //You can do anything on successful saving highlight list
                        }
                    });
                }
            }
        }).start();
    }

    private String loadAssetTextAsString(String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e("HomeActivity", "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e("HomeActivity", "Error closing asset " + name);
                }
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FolioReader.clear();
    }

    @Override
    public void onHighlight(HighLight highlight, HighLight.HighLightAction type) {
        Toast.makeText(this,
                "highlight id = " + highlight.getUUID() + " type = " + type,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFolioReaderClosed() {

        Log.v(LOG_TAG, "-> onFolioReaderClosed");
        this.finish();
    }


}