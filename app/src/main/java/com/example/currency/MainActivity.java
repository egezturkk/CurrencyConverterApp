package com.example.currency;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private NumberPicker pickerFrom, pickerTo;
    private EditText editAmount;
    private TextView textResult;
    private TextView fromCurrencyName, toCurrencyName;
    private Button buttonConvert;

    private String[] currencies = {
            "USD", "EUR", "GBP", "TRY", "AUD", "CAD", "JPY", "CHF", "INR", "BTC", "ETH", "CNY", "MXN", "BRL", "ZAR"
    };

    private Map<String, String> currencyNames = new HashMap<>();
    private Map<String, Double> exchangeRates = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        pickerFrom = findViewById(R.id.picker_from);
        pickerTo = findViewById(R.id.picker_to);
        editAmount = findViewById(R.id.edit_amount);
        textResult = findViewById(R.id.text_result);
        fromCurrencyName = findViewById(R.id.from_currency_name);
        toCurrencyName = findViewById(R.id.to_currency_name);
        buttonConvert = findViewById(R.id.button_convert);

        currencyNames.put("USD", "United States Dollar");
        currencyNames.put("EUR", "Euro");
        currencyNames.put("GBP", "British Pound");
        currencyNames.put("TRY", "Turkish Lira");
        currencyNames.put("AUD", "Australian Dollar");
        currencyNames.put("CAD", "Canadian Dollar");
        currencyNames.put("JPY", "Japanese Yen");
        currencyNames.put("CHF", "Swiss Franc");
        currencyNames.put("INR", "Indian Rupee");
        currencyNames.put("BTC", "Bitcoin");
        currencyNames.put("ETH", "Ethereum");
        currencyNames.put("CNY", "Chinese Yuan");
        currencyNames.put("MXN", "Mexican Peso");
        currencyNames.put("BRL", "Brazilian Real");
        currencyNames.put("ZAR", "South African Rand");

        buttonConvert.setOnClickListener(v -> convertCurrency());

        setCurrencyPickers();
        getExchangeRates();
    }

    private void setCurrencyPickers() {
        pickerFrom.setMinValue(0);
        pickerFrom.setMaxValue(currencies.length - 1);
        pickerFrom.setDisplayedValues(currencies);
        pickerFrom.setWrapSelectorWheel(true);

        pickerTo.setMinValue(0);
        pickerTo.setMaxValue(currencies.length - 1);
        pickerTo.setDisplayedValues(currencies);
        pickerTo.setWrapSelectorWheel(true);

        updateCurrencyNames();

        pickerFrom.setOnValueChangedListener((picker, oldVal, newVal) -> updateCurrencyNames());
        pickerTo.setOnValueChangedListener((picker, oldVal, newVal) -> updateCurrencyNames());
    }

    private void updateCurrencyNames() {
        String fromCurrency = pickerFrom.getDisplayedValues()[pickerFrom.getValue()];
        String toCurrency = pickerTo.getDisplayedValues()[pickerTo.getValue()];

        fromCurrencyName.setText(currencyNames.get(fromCurrency));
        toCurrencyName.setText(currencyNames.get(toCurrency));
    }

    private void getExchangeRates() {
        String apiUrl = "https://api.currencyfreaks.com/v2.0/rates/latest?apikey=YOUR_API_KEY_HERE&format=xml";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            parseExchangeRatesXML(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to get exchange rates", Toast.LENGTH_LONG).show();
        }
    }

    private void parseExchangeRatesXML(InputStream inputStream) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            String currentTag = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();

                        if (currentTag != null && Arrays.asList(currencies).contains(currentTag)) {
                            String rateValue = parser.nextText();
                            exchangeRates.put(currentTag, Double.parseDouble(rateValue));
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        currentTag = null;
                        break;
                }
                eventType = parser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing exchange rates XML", Toast.LENGTH_LONG).show();
        }
    }

    private void convertCurrency() {
        String fromCurrency = pickerFrom.getDisplayedValues()[pickerFrom.getValue()];
        String toCurrency = pickerTo.getDisplayedValues()[pickerTo.getValue()];
        String amount = editAmount.getText().toString();

        if (amount.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_LONG).show();
            return;
        }

        double amountValue = Double.parseDouble(amount);

        Double fromRate = exchangeRates.get(fromCurrency);
        Double toRate = exchangeRates.get(toCurrency);

        if (fromRate != null && toRate != null) {
            double convertedAmount = amountValue * (toRate / fromRate);
            textResult.setText(String.format("%.2f", convertedAmount));
        } else {
            Toast.makeText(MainActivity.this, "Rates not found for selected currencies", Toast.LENGTH_LONG).show();
        }
    }
}
