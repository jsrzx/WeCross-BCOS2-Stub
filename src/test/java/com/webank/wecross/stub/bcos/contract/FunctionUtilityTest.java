package com.webank.wecross.stub.bcos.contract;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.fisco.bcos.web3j.abi.FunctionEncoder;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.junit.Test;

public class FunctionUtilityTest {

    private static final String[] params = new String[] {"aa", "bb", "cc"};
    private static final String[] emptyParams = new String[0];
    private static final String[] nonParams = null;

    private static String funcName = "funcName";
    private static String funcSignature = "funcName(string[])";
    private static String funcNoneParamsSignature = "funcName()";
    private static String funcMethodId = FunctionEncoder.buildMethodId(funcSignature);
    private static String funcEmptyParamsMethodId = FunctionEncoder.buildMethodId(funcSignature);
    private static String funcNoneParamsMethodId =
            FunctionEncoder.buildMethodId(funcNoneParamsSignature);
    private static Function function = FunctionUtility.newFunction(funcName, params);
    private static Function emptyParamsFunction =
            FunctionUtility.newFunction(funcName, emptyParams);
    private static Function noneParamsFunction = FunctionUtility.newFunction(funcName, nonParams);

    @Test
    public void newFunctionTest() throws IOException {
        String abi = FunctionEncoder.encode(function);
        assertTrue(abi.startsWith(funcMethodId));
        assertTrue(funcName.equals(function.getName()));
        assertTrue(abi.startsWith(funcMethodId));
        assertTrue(function.getInputParameters().size() == 1);
        assertTrue(function.getOutputParameters().size() == 1);
    }

    @Test
    public void newFunctionWithEmptyParamsTest() throws IOException {
        String abi = FunctionEncoder.encode(emptyParamsFunction);
        assertTrue(funcName.equals(emptyParamsFunction.getName()));
        assertTrue(abi.startsWith(funcEmptyParamsMethodId));
        assertTrue(emptyParamsFunction.getInputParameters().size() == 1);
        assertTrue(emptyParamsFunction.getOutputParameters().size() == 1);
    }

    @Test
    public void newFunctionWithNonParamsTest() throws IOException {
        String abi = FunctionEncoder.encode(noneParamsFunction);
        assertTrue(abi.length() == FunctionUtility.MethodIDWithHexPrefixLength);
        assertTrue(funcName.equals(noneParamsFunction.getName()));
        assertTrue(abi.startsWith(funcNoneParamsMethodId));
        assertTrue(noneParamsFunction.getInputParameters().size() == 0);
        assertTrue(noneParamsFunction.getOutputParameters().size() == 1);
    }

    @Test
    public void convertToStringListTest() throws IOException {
        String abi = FunctionEncoder.encode(function);
        assertTrue(abi.startsWith(funcMethodId));

        List<Type> typeList =
                FunctionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.size() == params.length);
        for (int i = 0; i < params.length; i++) {
            assertEquals(params[i], resultList.get(i));
        }
    }

    @Test
    public void emptyParamsConvertToStringListTest() throws IOException {
        Function function = FunctionUtility.newFunction(funcName, emptyParams);
        String abi = FunctionEncoder.encode(function);
        assertTrue(abi.startsWith(funcEmptyParamsMethodId));

        assertTrue(funcName.equals(function.getName()));

        List<Type> typeList =
                FunctionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void noneParamsConvertToStringListTest() throws IOException {
        Function function = FunctionUtility.newFunction(funcName, nonParams);
        String abi = FunctionEncoder.encode(function);
        assertTrue(abi.startsWith(funcNoneParamsMethodId));
        assertTrue(funcName.equals(function.getName()));

        List<Type> typeList =
                FunctionReturnDecoder.decode(
                        abi.substring(FunctionUtility.MethodIDWithHexPrefixLength),
                        function.getOutputParameters());
        List<String> resultList = FunctionUtility.convertToStringList(typeList);
        assertTrue(resultList.isEmpty());
    }

    @Test
    public void decodeOutputTest() throws IOException {
        assertTrue(Objects.isNull(FunctionUtility.decodeOutput("0x")));
        assertTrue(Objects.isNull(FunctionUtility.decodeOutput("")));

        String abi1 = FunctionEncoder.encode(emptyParamsFunction);

        String[] output1 =
                FunctionUtility.decodeOutput(
                        "0x" + abi1.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertTrue(output1.length == 0);

        String abi2 = FunctionEncoder.encode(function);
        String[] output2 =
                FunctionUtility.decodeOutput(
                        abi2.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertTrue(output2.length == params.length);
        for (int i = 0; i < output2.length; ++i) {
            assertEquals(output2[i], params[i]);
        }

        String abi3 = FunctionEncoder.encode(noneParamsFunction);
        String[] output3 =
                FunctionUtility.decodeOutput(
                        abi3.substring(FunctionUtility.MethodIDWithHexPrefixLength));
        assertTrue(Objects.isNull(output3));
    }

    @Test
    public void decodeInputTest() throws IOException {
        assertTrue(Objects.isNull(FunctionUtility.decodeInput("0x")));

        assertTrue(Objects.isNull(FunctionUtility.decodeInput("")));

        String abi1 = FunctionEncoder.encode(emptyParamsFunction);
        String[] input1 = FunctionUtility.decodeInput(abi1);
        assertTrue(input1.length == 0);

        String abi2 = FunctionEncoder.encode(function);
        String[] input2 = FunctionUtility.decodeInput(abi2);
        assertTrue(input2.length == params.length);

        for (int i = 0; i < input2.length; ++i) {
            assertEquals(input2[i], params[i]);
        }

        String abi3 = FunctionEncoder.encode(noneParamsFunction);
        String[] input3 = FunctionUtility.decodeInput(abi1);
        assertTrue(input3.length == 0);
    }

    @Test
    public void decodeTransactionReceiptInputTest() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x1");
        String abi = FunctionEncoder.encode(function);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == params.length);
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(Objects.isNull(outputs));
    }

    @Test
    public void decodeTransactionReceiptInputTest0() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x0");
        String abi = FunctionEncoder.encode(emptyParamsFunction);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == emptyParams.length);
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(outputs.length == 0);
    }

    @Test
    public void decodeTransactionReceiptInputTest1() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x0");
        String abi = FunctionEncoder.encode(noneParamsFunction);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(Objects.isNull(inputs));
    }

    @Test
    public void decodeTransactionReceiptInputTest2() throws IOException {
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setStatus("0x16");

        String funcName = "funcName";
        String[] params = new String[] {"aa", "bb", "cc"};

        Function function = FunctionUtility.newFunction(funcName, params);
        String abi = FunctionEncoder.encode(function);
        receipt.setInput(abi);
        receipt.setOutput("0x" + abi.substring(10));
        String[] inputs = FunctionUtility.decodeInput(receipt);
        assertTrue(inputs.length == params.length);
        String[] outputs = FunctionUtility.decodeOutput(receipt);
        assertTrue(Objects.isNull(outputs));
    }
}
