package com.github.hintofbasil.hodl;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hintofbasil.hodl.coinSummaryList.CoinSummary;
import com.github.hintofbasil.hodl.SearchableSpinner.CoinSelectListAdapter;
import com.github.hintofbasil.hodl.database.CoinSummaryDbHelper;
import com.github.hintofbasil.hodl.database.CoinSummarySchema;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.math.BigDecimal;

public class CoinDetailsActivity extends Activity {

    EditText quantityEditText;
    ImageView coinImageView;
    TextView price;
    TextView ownedValue;
    Spinner coinSearchBox;
    Switch watchSwitch;

    CoinSummary coinSummary;

    ImageLoader imageLoader;

    FloatingActionButton saveButton;

    boolean trackAutoEnabledOnce;

    CoinSummaryDbHelper dbHelper;
    SQLiteDatabase coinSummaryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_details);

        dbHelper = new CoinSummaryDbHelper(this);
        coinSummaryDatabase = dbHelper.getWritableDatabase();

        trackAutoEnabledOnce = false;

        coinSummary = (CoinSummary) getIntent().getSerializableExtra("coinSummary");

        imageLoader = ImageLoader.getInstance();
        coinImageView = (ImageView) findViewById(R.id.coin_image);
        price = (TextView)findViewById(R.id.coin_price_usd);
        ownedValue = (TextView) findViewById(R.id.coin_owned_value);
        quantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        coinSearchBox = (Spinner) findViewById(R.id.coin_search_box);
        watchSwitch = (Switch) findViewById(R.id.coin_watch_switch);
        saveButton = (FloatingActionButton) findViewById(R.id.save);

        String sortOrder = CoinSummarySchema.CoinEntry.COLUMN_NAME_RANK + " ASC";
        Cursor cursor = coinSummaryDatabase.query(
                CoinSummarySchema.CoinEntry.TABLE_NAME,
                CoinSummarySchema.allProjection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        int coinNumber = cursor.getCount();
        CoinSummary[] coinNames = new CoinSummary[coinNumber];
        int i = 0;
        while (cursor.moveToNext()) {
            CoinSummary summary = CoinSummary.buildFromCursor(cursor);
            coinNames[i++] = summary;
        }

        int toShow = 0;
        for (i=0; i<coinNames.length; i++) {
            CoinSummary summary = coinNames[i];
            if (summary.getSymbol().equals(coinSummary.getSymbol())) {
                toShow = i;
            }
        }

        CoinSelectListAdapter coinSearchBoxAdapter = new CoinSelectListAdapter(
                this,
                R.layout.coin_select_spinner_dropdown_no_image,
                coinNames);
        coinSearchBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ownedValue.setVisibility(View.GONE);
                CoinSummary newCoinSummary = (CoinSummary) coinSearchBox.getItemAtPosition(position);
                CoinDetailsActivity.this.coinSummary = newCoinSummary;
                CoinDetailsActivity.this.setCoinData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        coinSearchBox.setAdapter(coinSearchBoxAdapter);
        coinSearchBox.setSelection(toShow);

        setCoinData();
    }

    @Override
    protected void onDestroy() {
        coinSummaryDatabase.close();
        dbHelper.close();
        super.onDestroy();
    }

    private void setCoinData() {

        quantityEditText.removeTextChangedListener(textWatcher);

        imageLoader.displayImage(coinSummary.getImageURL(128), coinImageView);

        if (coinSummary.getPriceUSD(false) != null) {
            price.setText(String.format("$%s", coinSummary.getPriceUSD(true)));
        } else {
            String text = getString(R.string.price_missing);
            price.setText(text);
        }

        if (coinSummary.getOwnedValue(false).signum() == 1) {
            ownedValue.setText(String.format("($%s)", coinSummary.getOwnedValue(true)));
            ownedValue.setVisibility(View.VISIBLE);
        }

        if (coinSummary.getQuantity() != null) {
            quantityEditText.setText(coinSummary.getQuantity().toString());
        } else {
            quantityEditText.setText("0");
        }

        watchSwitch.setChecked(coinSummary.isWatched());

        quantityEditText.addTextChangedListener(textWatcher);
    }

    public void onSubmit(View view) {
        String quantityString = quantityEditText.getText().toString();
        try {
            BigDecimal quantity = new BigDecimal(quantityString);
            coinSummary.setQuantity(quantity);
            coinSummary.setWatched(watchSwitch.isChecked());
            coinSummary.updateDatabase(coinSummaryDatabase, "quantity", "watched");
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
        }
    }

    public void onWatchToggled(View view) {
        saveButton.setVisibility(View.VISIBLE);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            CoinDetailsActivity.this.saveButton.setVisibility(View.VISIBLE);
            CoinDetailsActivity.this.ownedValue.setVisibility(View.VISIBLE);

            if (count > before && !trackAutoEnabledOnce) {
                trackAutoEnabledOnce = true;
                watchSwitch.setChecked(true);
            }
            try {
                CoinDetailsActivity.this.coinSummary.setQuantity(new BigDecimal(s.toString()));
                if (coinSummary.getOwnedValue(false).signum() == 1) {
                    ownedValue.setText(String.format("($%s)", coinSummary.getOwnedValue(true)));
                    ownedValue.setVisibility(View.VISIBLE);
                } else {
                    ownedValue.setVisibility(View.GONE);
                }
            } catch (NumberFormatException e) {
                ownedValue.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
