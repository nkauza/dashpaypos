package com.rubiblue.dashpaypos;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "DashpayModulePlugin")
public class DashpayModulePluginPlugin extends Plugin {

    private DashpayModulePlugin implementation = new DashpayModulePlugin();

    protected static final int REQUEST_CODE = 1; // Unique request code
    protected static final int PRINT_REQUEST_CODE = 2;
    private static final String PAYMENT_URI = "com.ar.dashpaypos";
    public static int tsn=1;
    public static String lastSentTsn="";
    private PluginCall mReturnResults;

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void getSerial(PluginCall call) {
        try {
            JSObject ret = new JSObject();
            String SerialNumber = Build.SERIAL;
            if (SerialNumber == "unknown") {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    SerialNumber = Build.getSerial();
                }
            }
            ret.put("value", SerialNumber);
            call.resolve(ret);
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("value", "unknown");
            call.resolve(ret);
        }
    }

    @PluginMethod()
    public void print(PluginCall call) {
        try {
            //Context context = this.getBridge().getActivity().getApplicationContext();
            String printString = call.getString("printString");
            String EXTRA_ORIGINATING_URI = call.getString("EXTRA_ORIGINATING_URI");
            Boolean NewActivityLaunchOption = call.getBoolean("NewActivityLaunchOption", false);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            List<ResolveInfo> resInfo = this.getContext().getPackageManager().queryIntentActivities(share, 0);
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains("com.dashpay.bridge") ||
                        info.activityInfo.name.toLowerCase().contains("com.dashpay.bridge")) {
                    share.putExtra(Intent.EXTRA_ORIGINATING_URI, EXTRA_ORIGINATING_URI);
                    share.putExtra("key", "Print");
                    share.putExtra("printString", printString);
                    share.setPackage(info.activityInfo.packageName);
                    JSObject ret = new JSObject();
                    if (NewActivityLaunchOption == false) {
                        startActivityForResult(call,Intent.createChooser(share, "Select"),"receiptPrintResult");
                        //startActivityForResult(call, Intent.createChooser(share, "Select"), PRINT_REQUEST_CODE);
                        ret.put("value", "printing");
                    } else {
                        startActivityForResult(call,Intent.createChooser(share, "Select"),"receiptPrintResult");
                        //startActivityForResult(call, Intent.createChooser(share, "Select"), PRINT_REQUEST_CODE);
                        //this.getBridge().getActivity().startActivityForResult(Intent.createChooser(share, "Select"), PRINT_REQUEST_CODE);
                        ret.put("value", "sent to printer");
                    }
                    call.resolve(ret);
                }
            }
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("value", "printing failed " + ex.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod()
    public void pay(PluginCall call) {
        try {
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            boolean found = false;
            share.setType("text/plain");
            List<ResolveInfo> resInfo = this.getContext().getPackageManager().queryIntentActivities(share, 0);
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(PAYMENT_URI) ||
                        info.activityInfo.name.toLowerCase().contains(PAYMENT_URI)) {
                    share.putExtra(Intent.EXTRA_ORIGINATING_URI, call.getString("EXTRA_ORIGINATING_URI"));
                    share.putExtra("TRANSACTION_TYPE", call.getString("TRANSACTION_TYPE"));
                    //share.putExtra("TRANSACTION_TYPE","REVERSE LAST");
                    share.putExtra("AMOUNT", call.getString("AMOUNT")); // 15.00
                    share.putExtra("ADDITIONAL_AMOUNT", call.getString("ADDITIONAL_AMOUNT"));
                    share.putExtra("OPERATOR_ID", call.getString("OPERATOR_ID"));
                    share.putExtra("REFERENCE_NUMBER", call.getString("REFERENCE_NUMBER"));
                    share.putExtra("TRANSACTION_ID", call.getString("TRANSACTION_ID"));
                    lastSentTsn = call.getString("TRANSACTION_ID");

                    share.putExtra("ALLOW_FALLBACK","No");

                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }

            if (!found) {
                JSObject ret = new JSObject();
                ret.put("value", "no dashpay pos");
                call.resolve(ret);
                return;
            }

            mReturnResults = call;
            startActivityForResult(call,Intent.createChooser(share, "Select"),"cardPaymentResult");
        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("value", "dashpay pos init failed " + ex.getMessage());
            call.resolve(ret);
        }
    }

    @ActivityCallback
    private void receiptPrintResult(PluginCall call, ActivityResult activityResult) {
        if (call == null) {
            if (mReturnResults == null)
                return;

            return;
        } else {
            mReturnResults = call;
        }
        JSObject ret = new JSObject();
        ret.put("value", "Print Successful");
        mReturnResults.resolve(ret);
    }
    @ActivityCallback
    private void cardPaymentResult(PluginCall call, ActivityResult activityResult) {
        if (call == null) {
            if(mReturnResults == null)
                return;

            return;
        }else{
            mReturnResults = call;
        }

        JSObject ret = new JSObject();

        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            Intent intent = activityResult.getData();
            // Handle the Intent

            String tid = intent.getStringExtra("TRANSACTION_ID");
            String result = intent.getStringExtra("RESULT");

            if (result.equals("APPROVED")) {
                //Toast.makeText(getActivity(),intent.getStringExtra("RESPONSE_CODE"),Toast.LENGTH_SHORT).show();
                String responseCode = intent.getStringExtra("RESPONSE_CODE");
                String authCode = intent.getStringExtra("AUTH_CODE");

                ret.put("result", result);

                ret.put("result", result);
                ret.put("displayTest", authCode);
                ret.put("responseCode", responseCode);
                ret.put("value", "APPROVED");
                mReturnResults.resolve(ret);

            } else if (result.equals("DECLINED")) {
                ret.put("value", "DECLINED");
                mReturnResults.resolve(ret);
            } else {
                ret.put("value", "FAILED");
                mReturnResults.resolve(ret);
            }
        }else if (activityResult.getResultCode() == Activity.RESULT_CANCELED) {
            ret.put("value", "Cancelled" + activityResult.getResultCode());
            mReturnResults.resolve(ret);
        }else{
            ret.put("value", "UNKNOWN RESULTS: " + activityResult.getResultCode());
            mReturnResults.resolve(ret);
        }
        // Do something with the result data
    }
/*
    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent intent) {
        super.handleOnActivityResult(requestCode,resultCode,intent);
        try {
            mReturnResults = getSavedCall();
            if (requestCode == REQUEST_CODE) {
                if (mReturnResults != null) {
                    JSObject ret = new JSObject();

                    if (resultCode == Activity.RESULT_OK) {

                        //String _Result = intent.getStringExtra("RESULT");
                        String tid = intent.getStringExtra("TRANSACTION_ID");
                        String result = intent.getStringExtra("RESULT");

                        if (result.equals("APPROVED")) {
                            //Toast.makeText(getActivity(),intent.getStringExtra("RESPONSE_CODE"),Toast.LENGTH_SHORT).show();
                            String responseCode = intent.getStringExtra("RESPONSE_CODE");
                            String authCode = intent.getStringExtra("AUTH_CODE");

                            ret.put("result", result);

                            ret.put("result", result);
                            ret.put("displayTest", authCode);
                            ret.put("responseCode", responseCode);
                            ret.put("value", "APPROVED");
                            mReturnResults.resolve(ret);

                        } else if (result.equals("DECLINED")) {
                            ret.put("value", "DECLINED");
                            mReturnResults.resolve(ret);
                        } else {
                            ret.put("value", "FAILED");
                            mReturnResults.resolve(ret);
                        }

                        new CountDownTimer(2000, 1000) { // 5000 = 5 sec

                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                            }
                        }.start();
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        ret.put("value", "Cancelled" + resultCode);
                        mReturnResults.resolve(ret);
                    }else{
                        ret.put("value", "UNKNOWN RESULTS: " + resultCode);
                        mReturnResults.resolve(ret);
                    }
                }else{
                    JSObject ret = new JSObject();
                    ret.put("value", "REQUEST_CODE not matching");
                    mReturnResults.resolve(ret);
                }
            }else {
                JSObject ret = new JSObject();
                ret.put("value", "REQUEST_CODE not matching");
                mReturnResults.resolve(ret);
            }
        }catch (Exception e){
            JSObject ret = new JSObject();
            ret.put("value", e.getMessage());
            mReturnResults.resolve(ret);
        }
    }*/
}
