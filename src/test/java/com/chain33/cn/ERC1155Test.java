package com.chain33.cn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

import cn.chain33.javasdk.client.RpcClient;
import cn.chain33.javasdk.model.AccountInfo;
import cn.chain33.javasdk.model.abi.FunctionEncoder;
import cn.chain33.javasdk.model.abi.TypeReference;
import cn.chain33.javasdk.model.abi.datatypes.AddressETH;
import cn.chain33.javasdk.model.abi.datatypes.DynamicArray;
import cn.chain33.javasdk.model.abi.datatypes.Function;
import cn.chain33.javasdk.model.abi.datatypes.Type;
import cn.chain33.javasdk.model.abi.datatypes.Utf8String;
import cn.chain33.javasdk.model.abi.datatypes.generated.Uint256;
import cn.chain33.javasdk.model.enums.SignType;
import cn.chain33.javasdk.model.protobuf.Transaction;
import cn.chain33.javasdk.model.rpcresult.QueryTransactionResult;
import cn.chain33.javasdk.utils.EvmUtil;
import cn.chain33.javasdk.utils.TransactionUtil;

/**
 * NFT ERC1155 发行和转让
 */
public class ERC1155Test {


    // TODO:需要设置参数 平行链所在服务器IP地址
    String ip = "172.22.16.179";
    // 平行链服务端口
    int port = 8921;
    RpcClient client = new RpcClient(ip, port);

    // TODO:需要设置参数 平行链名称，固定格式user.p.xxxx.样例中使用的名称叫parademo， 根据自己平行链名称变化。  这个名称一定要和平行链配置文件中的名称完全一致。
    String paraName = "user.p.parademo.";

    // TODO:需要设置参数 合约部署人（管理员）地址和私钥,地址下需要有YCC来缴纳手续费
    // 生成方式参考CommonUtil.createAccount()方法，私钥和地址一一对应
    String managerAddress = "0x4cb94044427edb06ae7aeb8e8dd6eba078c8bc0a";
    String managerPrivateKey = "7dfe80684f7007b2829a28c85be681304f7f4cf6081303dbace925826e2891d1";
//	String managerAddress = "替换成自己的地址，用下面createAccount方法生成";
//  String managerPrivateKey = "替换成自己的私钥，用下面createAccount方法生成,注意私钥千万不能泄漏";

    // TODO:需要设置参数 用户手续费代扣地址和私钥,地址下需要有BTY来缴纳手续费
    // 生成方式参考下面testCreateAccount方法，私钥和地址一一对应
    String withholdAddress = "0xfd89c32962f19bcea69b76093a64a03618cb33be";
    String withholdPrivateKey = "56d1272fcf806c3c5105f3536e39c8b33f88cb8971011dfe5886159201884763";

    // 用户A地址和私钥
    String useraAddress;
    String useraPrivateKey;

    // 用户B地址和私钥
    String userbAddress;
    String userbPrivateKey;

    /**
     * ERC1155合约部署，调用测试
     *
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Test
    public void testERC1155() throws Exception {


        // =======> step1： 为用户A和B生成私钥和地址
        AccountInfo infoA = CommonUtil.createAccount();
        useraAddress = infoA.getAddress();
        useraPrivateKey = infoA.getPrivateKey();

        AccountInfo infoB = CommonUtil.createAccount();
        userbAddress = infoB.getAddress();
        userbPrivateKey = infoB.getPrivateKey();

        // =======>  step2: 通过管理员部署合约，部署好之后，合约就运行区块链内存中，后续可以直接调用，不用每次都调用部署合约这一步操作（除非业务上有需要）
        // 部署合约, 参数： 平行链合约名， 签名地址，签名私钥
        String txhash = deployContract(managerAddress, managerPrivateKey);

        //通过查询交易获取合约地址的方式
        QueryTransactionResult txResult = client.queryTransaction(txhash);
        String contractAddr = EvmUtil.getContractAddr((JSONObject) JSONObject.toJSON(Arrays.stream(txResult.getReceipt().getLogs()).filter(log -> log.getTy() == 603).findFirst().get().getLog()));
        System.out.println("部署合约地址= " + contractAddr);

//        // 本地计算合约地址方法，结果和交易查询出来的是一样的
//        String contractAddress = AddressUtil.getContractAddress(txhash, AddressUtil.genAddress(managerPrivateKey, AddressType.ETH_ADDRESS), AddressType.ETH_ADDRESS);
//        System.out.println("本地计算出来的合约地址= " + contractAddress);

        // =======>  step3: 调用合约发行NFT,假设为2件游戏道具各生成100个NFT资产, id从10000开始
        int lenght = 2;
        // tokenId数组
        List<Uint256> idList = new ArrayList<Uint256>();
        // 同一个tokenid发行多少份
        List<Uint256> amountList = new ArrayList<Uint256>();
        // 每一个tokenid对应的URI信息（一般对应存放图片的描述信息，图片内容的一个url）
        List<Utf8String> uriList = new ArrayList<Utf8String>();
        for (int i = 0; i < lenght; i++) {
        	// 设置token id值
            idList.add(new Uint256(10000 + i));
            // 设置每个token发行的数量
            amountList.add(new Uint256(100));
            // token的uri信息（内容根据实际需求自定义）
            uriList.add(new Utf8String("{\"图片描述\":\"由xxx创作\";\"创作时间\":\"2022/12/25\";\"图片存放路径\":\"http://www.baidu.com\"}"));
        }
        // 构造合约调用, mint对应solidity合约里的方法名， useraAddress, ids, amounts这三项对应合约里的参数。  将NFT发行在用户A地址下
        String encodeParam = FunctionEncoder.encode(new Function("mint", Arrays.asList(new AddressETH(useraAddress), new DynamicArray(idList), new DynamicArray(amountList), new DynamicArray(uriList)), Collections.emptyList()));

        txhash = callContract(encodeParam, contractAddr, managerAddress, managerPrivateKey, paraName);
        System.out.println("调用合约交易哈希：" + txhash);
        // =======>  查询用户A地址下的余额
		List<Type> result = client.callEVMContractReadOnlyFunc(contractAddr, new Function("balanceOf", Arrays.asList(new AddressETH(useraAddress), idList.get(0)), Collections.singletonList(new TypeReference<Uint256>() {})));
        System.out.println("转账前用户A,NFT  ID:" + idList.get(0).getValue() + " balance: " + result.get(0).getValue());


        // =======>  从A地址向B地址转账,使用代扣交易
        // 用户A将第1个NFT中的50个转给用户B
        encodeParam = FunctionEncoder.encode(new Function("transferArtNFT", Arrays.asList(new AddressETH(userbAddress), idList.get(0), new Uint256(50)), Collections.emptyList()));

        // 构造转账交易体，先用用户A对此笔交易签名，
        String txEncode = EvmUtil.callEvmContractForYCC(encodeParam, contractAddr, "", 0, useraPrivateKey, paraName, 100000L);
        //查看预估费用
        long gas = client.queryEVMGas(paraName + "evm", txEncode, useraAddress);
        System.out.println("Gas fee is:" + gas);
        Transaction tx = new Transaction(txEncode);
        long fee = gas + 100000L;
        if (fee > 100000L) {
            tx.setFee(fee);
        }

        //查看链上实时费率
        long feeRate = client.getProperFeeRate(0, 0);
        System.out.println("链上实时费率为：" + feeRate);
        if (feeRate < 100000) {
            feeRate = 100000;
        }
        String noBalanceTx = TransactionUtil.createNoBalanceTxForYCC(tx.getTx(), withholdPrivateKey, useraPrivateKey, feeRate, paraName);
        System.out.println("代扣交易手续费设置为：" + new Transaction(noBalanceTx).getFee());
        String hash = client.submitTransaction(noBalanceTx);
        
        Thread.sleep(10000);
        String nextString = "";
        for (int tick = 0; tick < 20; tick++) {
            txResult = client.queryTransaction(hash);
            if (txResult == null) {
                Thread.sleep(3000);
                continue;
            }

            System.out.println("next:" + txResult.getTx().getNext());
            QueryTransactionResult nextResult = client.queryTransaction(txResult.getTx().getNext());
            System.out.println("ty:" + nextResult.getReceipt().getTyname());
            nextString = txResult.getTx().getNext();
            break;
        }

        txResult = client.queryTransaction(nextString);
        if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
            System.out.println("合约调用成功");

        } else {
            System.out.println("合约调用失败，一般失败原因可能是因为地址下手续费不够");
        }
        // =======>  查询用户A地址下的余额
        List<Type> results = client.callEVMContractReadOnlyFunc(contractAddr, new Function("balanceOf", Arrays.asList(new AddressETH(useraAddress), idList.get(0)), Collections.singletonList(new TypeReference<Uint256>() {})));
        System.out.println("转账后用户A, NFT id:" + idList.get(0).getValue() + " 余额：" + results.get(0).getValue());

        // =======>  查询用户B地址下的余额
        results = client.callEVMContractReadOnlyFunc(contractAddr, new Function("balanceOf", Arrays.asList(new AddressETH(userbAddress), idList.get(0)), Collections.singletonList(new TypeReference<Uint256>() {})));
        System.out.println("转账后用户B, NFT id:" + idList.get(0).getValue() + " 余额：" + results.get(0).getValue());

        // =======>  查询指定tokenid的uri信息
        results = client.callEVMContractReadOnlyFunc(contractAddr, new Function("uri", Arrays.asList(idList.get(0)), Collections.singletonList(new TypeReference<Utf8String>() {})));
        System.out.println("转账后用户B, NFT id:" + idList.get(0).getValue() + " URI ：" + results.get(0).getValue());

    }

    /**
     * Step2:部署合约
     *
     * @throws Exception
     */
    private String deployContract(String address, String privateKey) throws Exception {

        // 部署合约
        String txhash = "";
        QueryTransactionResult txResult = new QueryTransactionResult();

        // 估算部署合约GAS费,先构建未签名的evm合约部署交易
        String evmTx = EvmUtil.createEvmContractForYCC(CommonUtil.byteCode_Manager_1155, FunctionEncoder.encodeConstructor(Collections.emptyList()), "deploy erc1155 by manager.", "", "", paraName, 0);
        long gas = client.queryEVMGas(paraName + "evm", evmTx, address);
        System.out.println("部署合约的Gas fee是: " + gas);
        
        //查询链上实时的feeRate然后进行比较
        long feeRate = client.getProperFeeRate(0, 0);
        System.out.println("feeRate:" + feeRate);
        // 利用tx包装类,完成evm交易手续费的设置与签名
        Transaction tx = new Transaction(evmTx);
        tx.setFee(gas+100000);
        //设置链上费率，防止手续费不够
        tx.setFeeRate(feeRate);
        tx.sign(SignType.ETH_SECP256K1, privateKey);
        
        // 将构造并签好名的交易通过rpc接口发送到平行链上
        txhash = client.submitTransaction(tx.hexString());
        System.out.println("部署合约交易hash = " + txhash);

        // YCC平均1-5秒一个区块确认， 需要延时去查结果
        Thread.sleep(10000);
        for (int tick = 0; tick < 20; tick++) {
            txResult = client.queryTransaction(txhash);
            if (txResult == null) {
                Thread.sleep(3000);
                continue;
            }
            break;
        }

        if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
            System.out.println("合约部署成功");

        } else {
            System.out.println("合约部署失败, 通过浏览器或查询接口查询交易内容判断失败原因");
        }

        return txhash;
    }

    /**
     * 调用合约
     *
     * @param functionEncode
     * @param contractAddr
     * @param address
     * @param privateKey
     * @param paraName
     * @return
     * @throws Exception
     */
    private String callContract(String functionEncode, String contractAddr, String address, String privateKey, String paraName) throws Exception {

        // 调用合约
        String txhash = "";
        QueryTransactionResult txResult = new QueryTransactionResult();
        // 估算合约执行GAS费
        String evmTx = EvmUtil.callEvmContractForYCC(functionEncode, contractAddr, "", 0, "", paraName, TransactionUtil.PARA_CALL_EVM_FEE);
        long gas = client.queryEVMGas(paraName + "evm", evmTx, address);
        System.out.println("Gas fee is:" + gas);
        
        //查询链上实时的feeRate然后进行比较
        long feeRate = client.getProperFeeRate(0, 0);
        System.out.println("feeRate:" + feeRate);
        Transaction tx = new Transaction(evmTx);

        // 利用tx包装类,完成evm交易手续费的设置与签名
        //gas费用是估算的所以要加100000作为预留空间，充分保证交易不会因为手续费过低执行失败
        tx.setFee(gas+100000L);
        //设置链上费率，防止手续费不够
        tx.setFeeRate(feeRate);
        tx.sign(SignType.ETH_SECP256K1, privateKey);
        txhash = client.submitTransaction(tx.hexString());
        System.out.println("调用合约hash = " + txhash);

        // YCC平均1-5秒一个区块确认， 需要延时去查结果
        Thread.sleep(10000);
        for (int tick = 0; tick < 20; tick++) {
            txResult = client.queryTransaction(txhash);
            if (txResult == null) {
                Thread.sleep(3000);
                continue;
            }
            break;
        }

        if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
            System.out.println("合约调用成功");

        } else {
            System.out.println("合约调用失败，通过浏览器或查询接口查询交易内容判断失败原因");
        }

        return txhash;

    }


}
