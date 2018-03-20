package com.metasyntaxis.maxim.twoactivities;

import org.json.JSONObject;

/**
 * Created by maxim on 14.03.18.
 */

public interface IBooks {
    long insertBook(Book ob);
    String getBookInfo(long id);
    void updateBook(Book ob, int id);
    void deleteBook(long id);

}
