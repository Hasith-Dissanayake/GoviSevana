package lk.javainstitute.govisevana.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import lk.javainstitute.govisevana.model.ProductModel;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "goviSevana.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_IMAGE_URLS = "imageUrls";
    public static final String COLUMN_FARMER_NAME = "farmerName";
    public static final String COLUMN_FARMER_PHONE = "farmerPhone";
    public static final String COLUMN_IS_APPROVED = "isApproved";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_QUANTITY + " INTEGER,"
                + COLUMN_PRICE + " REAL,"
                + COLUMN_IMAGE_URLS + " TEXT,"
                + COLUMN_FARMER_NAME + " TEXT,"
                + COLUMN_FARMER_PHONE + " TEXT,"
                + COLUMN_IS_APPROVED + " BOOLEAN" + ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public void insertProduct(ProductModel product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, product.getTitle());
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        values.put(COLUMN_QUANTITY, product.getQuantity());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_IMAGE_URLS, TextUtils.join(",", product.getImageUrls()));
        values.put(COLUMN_FARMER_NAME, product.getFarmerName());
        values.put(COLUMN_FARMER_PHONE, product.getFarmerPhone());
        values.put(COLUMN_IS_APPROVED, product.isApproved());

        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
    }
}
