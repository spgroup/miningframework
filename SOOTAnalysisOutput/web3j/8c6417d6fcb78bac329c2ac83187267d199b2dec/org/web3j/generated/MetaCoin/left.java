package org.web3j.generated;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

public class MetaCoin extends Contract {

    private static final String BINARY = "0x6060604052341561000f57600080fd5b6127106000803273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055506103c5806100636000396000f300606060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680637bd703e81461005c57806390b98a11146100a9578063f8b2cb4f14610103575b600080fd5b341561006757600080fd5b610093600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610150565b6040518082815260200191505060405180910390f35b34156100b457600080fd5b6100e9600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919080359060200190919050506101f8565b604051808215151515815260200191505060405180910390f35b341561010e57600080fd5b61013a600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610351565b6040518082815260200191505060405180910390f35b600073__ConvertLib____________________________6396e4ee3d61017584610351565b60026000604051602001526040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808381526020018281526020019250505060206040518083038186803b15156101d657600080fd5b6102c65a03f415156101e757600080fd5b505050604051805190509050919050565b6000816000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020541015610249576000905061034b565b816000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008282540392505081905550816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600082825401925050819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190505b92915050565b60008060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205490509190505600a165627a7a72305820f791829bf0c920a6d43123cac9b9894757ddc838c17efbad6d56773d6e3dcf4c0029";

    public static final String FUNC_GETBALANCEINETH = "getBalanceInEth";

    public static final String FUNC_SENDCOIN = "sendCoin";

    public static final String FUNC_GETBALANCE = "getBalance";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<>();
        _addresses.put("4", "0xaea9d31a4aeda9e510f7d85559261c16ea0b6b8b");
    }

    protected MetaCoin(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MetaCoin(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<Uint256> getBalanceInEth(Address addr) {
        final Function function = new Function(FUNC_GETBALANCEINETH, Arrays.<Type>asList(addr), Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
        }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public RemoteCall<TransactionReceipt> sendCoin(Address receiver, Uint256 amount) {
        final Function function = new Function(FUNC_SENDCOIN, Arrays.<Type>asList(receiver, amount), Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Uint256> getBalance(Address addr) {
        final Function function = new Function(FUNC_GETBALANCE, Arrays.<Type>asList(addr), Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
        }));
        return executeRemoteCallSingleValueReturn(function);
    }

    public static RemoteCall<MetaCoin> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MetaCoin.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<MetaCoin> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(MetaCoin.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public List<TransferEventResponse> getTransferEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Transfer", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
        }, new TypeReference<Address>() {
        }), Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
        }));
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(event, transactionReceipt);
        ArrayList<TransferEventResponse> responses = new ArrayList<TransferEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransferEventResponse typedResponse = new TransferEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
            typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
            typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<TransferEventResponse> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Transfer", Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
        }, new TypeReference<Address>() {
        }), Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
        }));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, TransferEventResponse>() {

            @Override
            public TransferEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                TransferEventResponse typedResponse = new TransferEventResponse();
                typedResponse.log = log;
                typedResponse._from = (Address) eventValues.getIndexedValues().get(0);
                typedResponse._to = (Address) eventValues.getIndexedValues().get(1);
                typedResponse._value = (Uint256) eventValues.getNonIndexedValues().get(0);
                return typedResponse;
            }
        });
    }

    public static MetaCoin load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MetaCoin(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static MetaCoin load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MetaCoin(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class TransferEventResponse {

        public Log log;

        public Address _from;

        public Address _to;

        public Uint256 _value;
    }
}
