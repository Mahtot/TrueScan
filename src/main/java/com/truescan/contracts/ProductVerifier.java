package com.truescan.contracts;

import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Auto-generated smart contract wrapper.
 * <p><strong>Do not modify!</strong>
 * <p>Generated using web3j version 1.7.0.
 */
@SuppressWarnings("rawtypes")
public class ProductVerifier extends Contract {
    private static final String BINARY = "6080604052348015600e575f5ffd5b506102d3...";

    public static final String FUNC_REGISTERPRODUCT = "registerProduct";
    public static final String FUNC_ISPRODUCTREGISTERED = "isProductRegistered";

    public static final Event PRODUCTREGISTERED_EVENT = new Event(
            "ProductRegistered",
            Collections.singletonList(new TypeReference<Bytes32>() {})
    );

    // Constructors
    public ProductVerifier(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    public ProductVerifier(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // Contract methods
    public RemoteFunctionCall<TransactionReceipt> registerProduct(byte[] productHash) {
        Function function = new Function(
                FUNC_REGISTERPRODUCT,
                Collections.singletonList(new Bytes32(productHash)),
                Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> isProductRegistered(byte[] productHash) {
        Function function = new Function(
                FUNC_ISPRODUCTREGISTERED,
                Collections.singletonList(new Bytes32(productHash)),
                Collections.singletonList(new TypeReference<Bool>() {})
        );
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    // Event methods
    public static List<ProductRegisteredEventResponse> getProductRegisteredEvents(TransactionReceipt receipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PRODUCTREGISTERED_EVENT, receipt);
        List<ProductRegisteredEventResponse> responses = new ArrayList<>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            ProductRegisteredEventResponse response = new ProductRegisteredEventResponse();
            response.log = eventValues.getLog();
            response.productHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(response);
        }
        return responses;
    }

    public static ProductRegisteredEventResponse getProductRegisteredEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PRODUCTREGISTERED_EVENT, log);
        ProductRegisteredEventResponse response = new ProductRegisteredEventResponse();
        response.log = log;
        response.productHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        return response;
    }

    public Flowable<ProductRegisteredEventResponse> productRegisteredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(ProductVerifier::getProductRegisteredEventFromLog);
    }

    public Flowable<ProductRegisteredEventResponse> productRegisteredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PRODUCTREGISTERED_EVENT));
        return productRegisteredEventFlowable(filter);
    }

    // Deployment
    public static RemoteCall<ProductVerifier> deploy(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return deployRemoteCall(ProductVerifier.class, web3j, credentials, gasProvider, BINARY, "");
    }

    public static RemoteCall<ProductVerifier> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return deployRemoteCall(ProductVerifier.class, web3j, transactionManager, gasProvider, BINARY, "");
    }

    public static ProductVerifier load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new ProductVerifier(contractAddress, web3j, credentials, gasProvider);
    }

    public static ProductVerifier load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new ProductVerifier(contractAddress, web3j, transactionManager, gasProvider);
    }

    // Event response class
    public static class ProductRegisteredEventResponse extends BaseEventResponse {
        public byte[] productHash;
    }
}
