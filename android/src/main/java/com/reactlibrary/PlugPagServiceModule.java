package com.reactlibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.content.pm.PackageInfo;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPag;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagActivationData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagAppIdentification;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagEventListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagInitializationResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNFCResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagNearFieldCardData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPaymentData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrintResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterData;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagPrinterListener;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagTransactionResult;
import br.com.uol.pagseguro.plugpagservice.wrapper.PlugPagVoidData;

public class PlugPagServiceModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private PlugPagAppIdentification appIdentification;
    private PlugPag plugPag;
    private int countPrint = 0;
    private int countImages = 0;
    private String messageCard = null;
    private int countPassword = 0;
    private String getPassword = null;
    private PlugPagTransactionResult result = null;

    private PackageInfo getPackageInfo() throws Exception {
        return getReactApplicationContext().getPackageManager().getPackageInfo(getReactApplicationContext().getPackageName(), 0);
    }

    public PlugPagServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "PlugPagService";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        constants.put("PAYMENT_CREDITO", PlugPag.TYPE_CREDITO);
        constants.put("PAYMENT_DEBITO", PlugPag.TYPE_DEBITO);
        constants.put("PAYMENT_VOUCHER", PlugPag.TYPE_VOUCHER);

        constants.put("INSTALLMENT_TYPE_A_VISTA", PlugPag.INSTALLMENT_TYPE_A_VISTA);
        constants.put("INSTALLMENT_TYPE_PARC_VENDEDOR", PlugPag.INSTALLMENT_TYPE_PARC_VENDEDOR);
        constants.put("INSTALLMENT_TYPE_PARC_COMPRADOR", PlugPag.INSTALLMENT_TYPE_PARC_COMPRADOR);

        constants.put("OPERATION_ABORT", PlugPag.OPERATION_ABORT);
        constants.put("OPERATION_ABORTED", PlugPag.OPERATION_ABORTED);
        constants.put("OPERATION_ACTIVATE", PlugPag.OPERATION_ACTIVATE);
        constants.put("OPERATION_CALCULATE_INSTALLMENTS", PlugPag.OPERATION_CALCULATE_INSTALLMENTS);
        constants.put("OPERATION_CHECK_AUTHENTICATION", PlugPag.OPERATION_CHECK_AUTHENTICATION);
        constants.put("OPERATION_DEACTIVATE", PlugPag.OPERATION_DEACTIVATE);
        constants.put("OPERATION_GET_APPLICATION_CODE", PlugPag.OPERATION_GET_APPLICATION_CODE);
        constants.put("OPERATION_GET_LIB_VERSION", PlugPag.OPERATION_GET_LIB_VERSION);
        constants.put("OPERATION_GET_READER_INFOS", PlugPag.OPERATION_GET_READER_INFOS);
        constants.put("OPERATION_GET_USER_DATA", PlugPag.OPERATION_GET_USER_DATA);
        constants.put("OPERATION_HAS_CAPABILITY", PlugPag.OPERATION_HAS_CAPABILITY);
        constants.put("OPERATION_INVALIDATE_AUTHENTICATION", PlugPag.OPERATION_INVALIDATE_AUTHENTICATION);
        constants.put("OPERATION_NFC_ABORT", PlugPag.OPERATION_NFC_ABORT);
        constants.put("OPERATION_NFC_READ", PlugPag.OPERATION_NFC_READ);
        constants.put("OPERATION_NFC_WRITE", PlugPag.OPERATION_NFC_WRITE);
        constants.put("OPERATION_PAYMENT", PlugPag.OPERATION_PAYMENT);
        constants.put("OPERATION_PRINT", PlugPag.OPERATION_PRINT);
        constants.put("OPERATION_QUERY_LAST_APPROVED_TRANSACTION", PlugPag.OPERATION_QUERY_LAST_APPROVED_TRANSACTION);
        constants.put("OPERATION_REFUND", PlugPag.OPERATION_REFUND);
        constants.put("OPERATION_REPRINT_CUSTOMER_RECEIPT", PlugPag.OPERATION_REPRINT_CUSTOMER_RECEIPT);
        constants.put("ACTION_POST_OPERATION", PlugPag.ACTION_POST_OPERATION);
        constants.put("ACTION_PRE_OPERATION", PlugPag.ACTION_PRE_OPERATION);
        constants.put("ACTION_UPDATE", PlugPag.ACTION_UPDATE);
        constants.put("APN_SERVICE_CLASS_NAME", PlugPag.APN_SERVICE_CLASS_NAME);
        constants.put("APN_SERVICE_PACKAGE_NAME", PlugPag.APN_SERVICE_PACKAGE_NAME);
        constants.put("AUTHENTICATION_FAILED", PlugPag.AUTHENTICATION_FAILED);
        constants.put("COMMUNICATION_ERROR", PlugPag.COMMUNICATION_ERROR);
        constants.put("ERROR_CODE_OK", PlugPag.ERROR_CODE_OK);
        constants.put("MIN_PRINTER_STEPS", PlugPag.MIN_PRINTER_STEPS);
        constants.put("NFC_SERVICE_CLASS_NAME", PlugPag.NFC_SERVICE_CLASS_NAME);
        constants.put("NFC_SERVICE_PACKAGE_NAME", PlugPag.NFC_SERVICE_PACKAGE_NAME);
        constants.put("NO_PRINTER_DEVICE", PlugPag.NO_PRINTER_DEVICE);
        constants.put("NO_TRANSACTION_DATA", PlugPag.NO_TRANSACTION_DATA);
        constants.put("SERVICE_CLASS_NAME", PlugPag.SERVICE_CLASS_NAME);
        constants.put("SERVICE_PACKAGE_NAME", PlugPag.SERVICE_PACKAGE_NAME);
        constants.put("SMART_RECHARGE_SERVICE_CLASS_NAME", PlugPag.SMART_RECHARGE_SERVICE_CLASS_NAME);
        constants.put("SMART_RECHARGE_SERVICE_PACKAGE_NAME", PlugPag.SMART_RECHARGE_SERVICE_PACKAGE_NAME);

        constants.put("RET_OK", PlugPag.RET_OK);

        String appVersion;

        try {
            appVersion = getPackageInfo().versionName;
        } catch (Exception e) {
            appVersion = "unkown";
        }

        constants.put("appVersion", appVersion);


        return constants;
    }

    // Cria a identificação do aplicativo
    @ReactMethod
    public void setAppIdendification(String name, String version) {
        appIdentification = new PlugPagAppIdentification(name, version);
        plugPag = new PlugPag(reactContext, appIdentification);
    }
    

    // Ativa terminal e faz o pagamento
    @ReactMethod
    public void initializeAndActivatePinpad(String activationCode, Promise promise) {
        final PlugPagActivationData activationData = new PlugPagActivationData(activationCode);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<PlugPagInitializationResult> callable = new Callable<PlugPagInitializationResult>() {
            @Override
            public PlugPagInitializationResult call() throws Exception {
                return plugPag.initializeAndActivatePinpad(activationData);
            }
        };

        Future<PlugPagInitializationResult> future = executor.submit(callable);
        executor.shutdown();

        try {
            PlugPagInitializationResult initResult = future.get();

            final WritableMap map = Arguments.createMap();
            map.putInt("retCode", initResult.getResult());

            promise.resolve(map);
        } catch (ExecutionException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        } catch (InterruptedException e) {
            Log.d("PlugPag", e.getMessage());
            promise.reject("error", e.getMessage());
        }
    }

    @ReactMethod
    public void doPayment(String jsonStr, final Promise promise) {
        final PlugPagPaymentData paymentData = JsonParseUtils.getPlugPagPaymentDataFromJson(jsonStr);

        plugPag.setEventListener(new PlugPagEventListener() {
            @Override
            public void onEvent(final PlugPagEventData plugPagEventData) {
                messageCard = plugPagEventData.getCustomMessage();
                int code = plugPagEventData.getEventCode();


                if (code == PlugPagEventData.EVENT_CODE_WAITING_CARD) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_PIN_REQUESTED) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_PIN_OK) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_REMOVED_CARD) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_SALE_APPROVED) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_SALE_NOT_APPROVED) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_AUTHORIZING) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_INSERTED_CARD) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_SALE_END) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_WAITING_REMOVE_CARD) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.EVENT_CODE_DEFAULT) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (code == PlugPagEventData.ON_EVENT_ERROR) {
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", messageCard);
                } else if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD || plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
                    if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_DIGIT_PASSWORD) {
                        countPassword++;
                    } else if (plugPagEventData.getEventCode() == PlugPagEventData.EVENT_CODE_NO_PASSWORD) {
                        countPassword = 0;
                    }

                    if (countPassword == 0 ) {
                        getPassword = "Senha:";
                    } else if (countPassword == 1) {
                        getPassword = "Senha: *";
                    } else if (countPassword == 2) {
                        getPassword = "Senha: **";
                    } else if (countPassword == 3) {
                        getPassword = "Senha: ***";
                    } else if (countPassword == 4) {
                        getPassword = "Senha: ****";
                    } else if (countPassword == 5) {
                        getPassword = "Senha: *****";
                    } else if (countPassword == 6 || countPassword > 6) {
                        getPassword = "Senha: ******";
                    }
                    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("eventPayments", getPassword);
                }
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                PlugPagTransactionResult transactionResult = plugPag.doPayment(paymentData);
                final WritableMap map = Arguments.createMap();
                map.putInt("retCode", transactionResult.getResult());
                map.putString("transactionCode", transactionResult.getTransactionCode());
                map.putString("transactionId", transactionResult.getTransactionId());
                map.putString("message", transactionResult.getMessage());

                map.putInt("code", transactionResult.getResult());
                map.putString("amount", transactionResult.getAmount());
                map.putString("bin", transactionResult.getBin());
                map.putString("cardApplication", transactionResult.getCardApplication());
                map.putString("cardBrand", transactionResult.getCardBrand());
                map.putString("errorCode", transactionResult.getErrorCode());

                promise.resolve(map);
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    @ReactMethod
    public void reprintStablishmentReceipt(Promise promise) {
        PlugPagPrintResult result = plugPag.reprintStablishmentReceipt();
        promise.resolve(result.getResult());
    }

    @ReactMethod
    public void reprintCustomerReceipt(Promise promise) {
        PlugPagPrintResult result = plugPag.reprintCustomerReceipt();
        promise.resolve(result.getResult());
    }

    @ReactMethod
    public void reversePayment(final String code, final String id, final Promise promise) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                PlugPagTransactionResult transactionResult = plugPag.voidPayment(new PlugPagVoidData(code, id, true));
                final WritableMap map = Arguments.createMap();
                map.putInt("retCode", transactionResult.getResult());
                map.putString("message", transactionResult.getMessage());
                promise.resolve(map);
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    /*Método para pegar o serial do pos*/
    @ReactMethod
    public void getSerialNumber(Promise promise) throws NoSuchFieldException, IllegalAccessException {
        String deviceSerial = (String) Build.class.getField("SERIAL").get(null);
        promise.resolve(deviceSerial);
    }

    /* Método para ler ID do cartão*/
    @ReactMethod
    public void readNFCCardClean(int slot, Promise promise) throws UnsupportedEncodingException {
        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        PlugPagNFCResult result = plugPag.readFromNFCCard(dataCard);
        String returnValue = new String(result.getSlots()[result.getStartSlot()].get("data"), "UTF-8");

        if (result.getResult() == -1) {
            promise.resolve(null);
        } else {
            promise.resolve(returnValue);
        }
    }

    /* Método para escrever ID no cartão*/
    @ReactMethod
    public void writeToNFCCardClean(int slot, String info, Promise promise) {
        byte[] bytes = info.getBytes();

        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        dataCard.getSlots()[slot].put("data", bytes);

        PlugPagNFCResult result = plugPag.writeToNFCCard(dataCard);
        int returnResult = result.getResult();
        promise.resolve(returnResult);
    }

    /* Método para ler qualquer slot do cartão com hash*/
    @ReactMethod
    public void readNFCCard(int slot, Promise promise) throws UnsupportedEncodingException {
        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        PlugPagNFCResult result = plugPag.readFromNFCCard(dataCard);
        String returnValue = new String(result.getSlots()[result.getStartSlot()].get("data"), "UTF-8");
        byte[] data2 = Base64.decode(returnValue, Base64.DEFAULT);
        String text = new String(data2, StandardCharsets.UTF_8);
        promise.resolve(text);
    }

    /* Método para escrever qualquer slot do cartão com hash*/
    @ReactMethod
    public void writeToNFCCard(int slot, String info, Promise promise) throws UnsupportedEncodingException {
        byte[] bytes = info.getBytes("UTF-8");
        String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        byte[] bytesWrite = base64.getBytes("UTF-8");

        PlugPagNearFieldCardData dataCard = new PlugPagNearFieldCardData();
        dataCard.setStartSlot(slot);
        dataCard.setEndSlot(slot);
        dataCard.getSlots()[slot].put("data", bytesWrite);

        PlugPagNFCResult result = plugPag.writeToNFCCard(dataCard);
        int returnResult = result.getResult();
        promise.resolve(returnResult);
    }

    @ReactMethod
    public void cancelOperation() {
        plugPag.abort();
    }

    @ReactMethod
    public void printFile(final Promise promise) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnableTask = new Runnable() {
            @Override
            public void run() {
                setAppIdendification("pdv365", "0.0.1");
                countPrint = 0;
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/");
                File[] arquivos = file.listFiles();
                countImages = arquivos.length;


                for (File fileTmp : arquivos) {

                    final PlugPagPrinterData data = new PlugPagPrinterData( Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + fileTmp.getName(), 4, 10 * 12);

                    PlugPagPrinterListener listener = new PlugPagPrinterListener() {
                        @Override
                        public void onError(PlugPagPrintResult plugPagPrintResult) {
                            promise.reject("error", plugPagPrintResult.getMessage());
                        }

                        @Override
                        public void onSuccess(PlugPagPrintResult plugPagPrintResult) {
                            countPrint++;
                            if (countPrint == countImages) {
                                promise.resolve(null);
                            }
                        }
                    };

                    plugPag.setPrinterListener(listener);

                    plugPag.printFromFile(data);
                    fileTmp.delete();
                }
            }
        };

        executor.execute(runnableTask);
        executor.shutdown();
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable boolean params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("connectionEvent", params);
    }

    @ReactMethod
    public void connection() {
        ConnectivityManager conn = (ConnectivityManager)reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conn.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null ? activeNetwork.isConnectedOrConnecting() : false;

        sendEvent(reactContext, "connectionEvent", isConnected);
    }

    @ReactMethod
    public void cancelReadCard(Promise promise) {
        PlugPagNFCResult result =  plugPag.abortNFC();

        if (result.getResult() == -1) {
            promise.reject(null, "Não foi possível cancelar a operação.");
        } else {
            promise.resolve(null);
        }
    }

    // Calcular parcelas
    @ReactMethod
    public void calculateInstallments(String saleValue, final Promise promise) {
        try {
            setAppIdendification("pdv365", "0.0.1");
            String[] installments = plugPag.calculateInstallments(saleValue);
            WritableArray nativeArray = Arguments.fromList(Arrays.asList(installments));
            promise.resolve(nativeArray);
        }
        catch(Exception e){
            promise.reject(e);
        }
    }

}
