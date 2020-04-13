package com.webank.wecross.stub.bcos.contract;

import java.math.BigInteger;
import org.fisco.bcos.channel.client.Merkle;
import org.fisco.bcos.channel.client.ReceiptEncoder;
import org.fisco.bcos.web3j.crypto.Hash;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceiptWithProof;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionWithProof;
import org.fisco.bcos.web3j.rlp.RlpEncoder;
import org.fisco.bcos.web3j.rlp.RlpString;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProofVerifierUtility {

    private static final Logger logger = LoggerFactory.getLogger(ProofVerifierUtility.class);

    public static boolean verify(
            String transactionRoot,
            String receiptRoot,
            TransactionWithProof.TransAndProof transAndProof,
            TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {
        return verifyTransaction(transactionRoot, transAndProof)
                && verifyTransactionReceipt(receiptRoot, receiptAndProof);
    }

    public static boolean verifyTransaction(
            String transactionRoot, TransactionWithProof.TransAndProof transAndProof) {
        // transaction index
        Transaction transaction = transAndProof.getTransaction();
        BigInteger index = transaction.getTransactionIndex();
        String input =
                Numeric.toHexString(RlpEncoder.encode(RlpString.create(index)))
                        + transaction.getHash().substring(2);
        String proof = Merkle.calculateMerkleRoot(transAndProof.getTxProof(), input);

        logger.debug(
                " transaction hash: {}, transaction index: {}, root: {}, proof: {}",
                transaction.getHash(),
                transaction.getTransactionIndex(),
                transactionRoot,
                proof);

        boolean verifyOk = proof.equals(transactionRoot);

        if (!verifyOk) {
            logger.warn(
                    " transaction verify failed, hash: {}, index: {}, transaction root: {}, proof:{}",
                    transaction.getHash(),
                    transaction.getTransactionIndex(),
                    transactionRoot,
                    proof);
        }

        return verifyOk;
    }

    public static boolean verifyTransactionReceipt(
            String receiptRoot, TransactionReceiptWithProof.ReceiptAndProof receiptAndProof) {

        TransactionReceipt transactionReceipt = receiptAndProof.getTransactionReceipt();

        // transaction index
        byte[] byteIndex =
                RlpEncoder.encode(RlpString.create(transactionReceipt.getTransactionIndex()));

        if (!transactionReceipt.getGasUsedRaw().startsWith("0x")) {
            transactionReceipt.setGasUsed("0x" + transactionReceipt.getGasUsed().toString(16));
        }

        String receiptRlp = ReceiptEncoder.encode(transactionReceipt);
        String rlpHash = Hash.sha3(receiptRlp);
        String input = Numeric.toHexString(byteIndex) + rlpHash.substring(2);

        String proof = Merkle.calculateMerkleRoot(receiptAndProof.getReceiptProof(), input);

        logger.debug(
                " transaction hash: {}, receipt index: {}, root: {}, proof: {}, receipt: {}",
                transactionReceipt.getTransactionHash(),
                transactionReceipt.getTransactionIndex(),
                receiptRoot,
                proof,
                receiptAndProof.getTransactionReceipt());

        boolean verifyOk = proof.equals(receiptRoot);

        if (!verifyOk) {
            logger.warn(
                    " transaction receipt verify failed, transaction receipt: {}, receipt root: {}, proof:{}",
                    transactionReceipt,
                    receiptRoot,
                    proof);
        }

        return verifyOk;
    }
}