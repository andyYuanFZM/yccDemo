# YCC+平行链EVM合约使用教程

## 文档目录
	- [文档修改记录](#文档修改记录)
	- [文档阅读说明](#文档阅读说明)
	- [术语介绍](#术语介绍)
	- [背景介绍](#背景介绍)
	- [主链和平行链环境部署](#主链和平行链环境部署)
	- [EVM合约概述](#EVM合约概述)
	- [合约部署调用](#合约部署调用)
	- [应用和YCC平行链对接注意事项](#应用和YCC平行链对接注意事项)

### 文档修改记
| 版本号 | 版本描述                              | 修改日期   | 备注 |
| ------ | ------------------------------------- | ---------- | ---- |
| V1.0   | 1. 主链+平行链部署<br>2. NFT合约简单概述<br>3.通过remixIDE和小狐狸插件部署合约<br>4. 通过web3.js, truffle将一个以太坊上的应用部署到YCC平行链上<br>5. 对接注意事项| 2022/06/29|

### 文档阅读说明
- 术语介绍：简单了解  
- 背景介绍: 简单了解  
- 主链和平行链环境部署：已经具备环境情况下，可忽略；***还未部署的重点了解。***
- EVM合约概述： 了解下概念，文档中有NFT合约的样例，如果样例太简单满足不了业务需求，需要自己开发新合约的，可以重点了解  
- 合约部署调用：***重点了解***
- 应用和YCC平行链对接注意事项: ***重点了解，是项目中踩过的坑，了解过后可少走弯路。***

### 术语介绍 
| 序号 | 术语 缩写                              | 解释   |
| ------- | -------------------------------------- | --------------------- |
| 1   | YCC主链| YCC主链采用POS33共识机制，共识效率高，且节点可随意加入和退出集群|
| 2   | 平行链| 平行链依附于主链，平行链之间通过名称来区分，平行链与平行链之间数据相互隔离， 平行链与主链之间通过grpc通信。|
| 3   | EVM| 以太坊虚拟机的缩写，目前EVM算是区块链中最大的生态，很多链都支持EVM的能力，森田平行链也完全兼容EVM,通过EVM可以动态的部署智能合约进行计算|
| 4   | ERC721| 运行在EVM中，服务于非同质化代币（NFT）, 每个Token都是不一样的，都有自己的唯一性和独特价值,不可分割，可追踪。|
| 5   | ERC1155| 运行在EVM中，也是服务于非同质化代币(NFT),相比于ERC721它同时还支持在一个合约中存储多个数字资产，支持一次性批量发行多个不同类型的的数字资产，支持在一次转账过程中转多个不同类型的数字资产。|
| 6   | 交易组| 把两笔及以上的交易放在一个组里一次性发送。|
| 7   | 代扣手续费| 将代扣交易和正常用户的交易打包进一个交易组中，代扣交易使用代扣地址签名，用于主链上手续费扣除。（适用于能过java-sdk和go-sdk对接的方式，不适用于web3.js对接方式）|
| 8   | SDK| 封装了同区块链交互的接口和区块链通用方法（包括：公私钥生成，签名，交易构造等）, 支持java-sdk, go-sdk, web3.js等 |

### 背景介绍
YCC公链和平行链都是基于Chain33区块链开发框架开发出来的，主链和平行链的的区别就是加载了不同的共识机制插件。      
- POS33共识插件：YCC主链共识插件，采用抽签投票方式pos共识算法. 
- PARA共识插件：平行链共识插件，平行链不是独立存在的，它依附于主链（上述的5种都是主链），利用主链的共识算法来保证其安全性，同时平行链实现交易执行分片，主链下可以挂很多不同名称的平行链，每条平行链只负责自己独立的业务。 平行链条数的增加不会影响主链的性能，也不会影响其它平行链的性能。 比如EVM合约运行在平行链上， 而主链上只对这些交易的原始信息做共识和存证，所以主链只做存证而不用做具体的计算性能就可以很高。   

### 主链和平行链环境部署
#### 说明  
主链采用抽签投票POS共识机制, 抽签的目的是减少投票人的数量. 每轮进行两次抽签, 第一次抽出候选的出块人, 第二次抽出投票人. 然后由投票人对候选的区块制作人投票, 谁的票多谁就制作新区块. 为了全网共识安全性, ycc节点需要提前抵押部分ycc才有参与共识的资格. ycc采用vrf 算法抽签, 根据自己的密钥以及区块高度和其他公共信息, 计算一个随机数, 如果此随机数小于某个给定的数值, 表示抽取成功. 为了减少出块时间, 系统提前进行抽签和投票, 这样出块人可以提前准备好出块的数据. 无需在本轮等待抽签和投票数据. 
通过以上机制 ycc 拥有较高的TPS。   
主链区块链浏览器： [区块链浏览器地址](https://mainnet.yuan.org/block)   
主链+平行链交易流程：  
- 交易在链下完成构造和签名,交易构造时需要在交易体中带上对应平行链的名称。   
- 签好名的交易通过平行链的jsonrpc接口发往平行链节点。   
- 平行链通过它和主链之间的grpc连接,将交易转发到主链节点,由主链打包区块共识后存入主链账本。   
- 主链区块生成后,平行链实时拉取新产生的区块,过滤出属于本平行链的交易（根据平行链名称）, 送入虚拟机执行后并写入平行链账本。  
下面介绍主链节点和平行链节点的部署，智能合约部署和调用方法。  

*注： 支持在同一台服务器上同时部署BTY主链节点和BTY平行链节点（只要保证两者的jsonrpc和grpc端口不冲突即可）*
	
#### 主链节点同步：  
待补充
 
#### 平行链节点部署 
待补充

### EVM合约概述
合约运行在平行链的EVM虚拟机中, EVM虚拟机运行solidity语言编写和编译的智能合约。   
Solidity语言更多信息, 请参阅  [[Solidity中文官方文档]](https://learnblockchain.cn/docs/solidity/)  
下文[NFT合约说明]链接介绍基于EVM的ERC1155和ERC721两类非同质化通证合约最简单的使用。  
合约的基本介绍 [[NFT合约说明]](https://github.com/andyYuanFZM/yccDemo/blob/master/NFT合约说明.md)   

### 合约部署调用     
#### 通过web3.js方式 
YCC平行链除了兼容以太坊虚拟机（EVM）,同时在接口上也做了适配， 完全兼容web3.js，小狐狸插件（metamask），truffle, remix等以太坊生态工具。

##### 通过remix和metamask在YCC平行链上部署调用NFT合约  
1.  浏览器安装metamask插件，创建账户后添加YCC平行链的rpc连接  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/metamask.png)    
新增RPC URL:  此处改成用户自己YCC平行链的rpc链接    
链ID: 使用YCC注册的固定值 3999    

2. 账户下充一些YCC用于手续费支付（TODO: 获取YCC方式待补充）

3. 编译部署合约
- **准备测试合约**：可以参考 [[ERC1155合约]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC1155ByManager.sol)     
- **准备合约编译器**：打开在线remix IDE:  https://remix.ethereum.org/   
- **新建工程文件**：在remix的workspace下，新建一个ERC1155.sol文件，并将上述参考合约内容黏贴进去。  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix1.png)   
- **编译合约**：选用的编译器版本要大于等于合约中要求的版本
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix2.png)   
- **metamask连接remix**：环境选择[Injected Web3] -- 弹出metamask插件，选择[确认]连接， 连接成功后metemask中配置的chainID和用户账户地址都会在remix界面中显示。  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix3.png)   
- **部署合约到YCC平行链上**：选择[deploy]按钮 -- 弹出matemask插件，提示部署合约所需的GAS费 -- 确保自己账户下燃料充足 -- 点击确认-- 成功后会在控制台打印合约部署信息  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix4.png)   
左侧红框中Deployed Contracts:  部署好的合约地址  
status: 合约部署的结果  
transaction hash: 合约部署成功返回的hash值  
from: 部署人的地址  
gas: 部署合约需要的gas  
- **调用合约**：点击[Deployed Contracts]边上下拉箭头，再点击[mint]函数边上的下拉箭头，输入mint函数所需的参数(注意数据类型要和solidity合约中定义的保持一致)   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix5.png)   
- **查询结果**：点击[Deployed Contracts]边上下拉箭头，再点击[balanceof]函数边上的下拉箭头，输入balanceof函数所需的参数,点击[call]按钮查询  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix6.png)   

##### 通过truffle和metamask在YCC平行链上部署一个以太坊应用  
我们用以太坊上的一个应用（NFT-Marketplace）为例，说明如何把它快速移植到YCC平行链上。  
NFT-Marketplace开源地址：https://github.com/BravoNatalie/NFT-Marketplace， 实现了一个Demo性质的去中心化NFT交易市场，可以发行也可以买卖NFT。 

1. 环境准备  
- 产品的客户端+服务端（运行在Win10机器上, 机器上的visual studio版本大于等于 2015）   
- 区块链(YCC主链+平行链运行在linux服务器上)    
- python3   
- Git工具   
- Node   
- Yarn或npm   
- Truffle  
- metamask浏览器插件 

2. 环境部署  
- 通过git bash工具下载NFT市场开源代码  
```
git clone https://github.com/BravoNatalie/NFT-Marketplace.git  
```

- 通过yarn命令安装依赖（保证网络没有限制，如果有些依赖下载失败，可能需要科学上网） 
```
# 进入目录
cd NFT-Marketplace  
# 执行yarn命令
yarn  
```

- 修改truffle配置文件  
```
# 在NFT-Marketplace目录下，修改truffle-config.js文件，
# 1. 在文件头增加const定义
const HDWalletProvider = require('@truffle/hdwallet-provider');
# 2. 增加以下YCC平行链网络的配置
    ycc: {
      network_id: "3999",       // YCC  network_id： 对应YCC的chainid，固定填写3999
      from: "0xc05109180ac5298e3a9b7d7e70abf98ffb986d22",  // 用户地址，需要保证地址下有燃料
      provider: function() {
        return new HDWalletProvider(mnemonic , "http://121.52.224.92:8546/"); // mnemonic：钱包助记词; url: YCC平行链对应的rpc地址
      }
    },
```

- 运行truffle工具，将./contracts/目录下的三个合约部署到链上  
```
# 修改PATH环境变量，将node_modules/.bin加到PATH中
export PATH=$(pwd)/node_modules/.bin:$PATH
# 删除之前已经编译好的文件
cd client\src\contracts
rm -rf *
# 再回到NFT-Marketplace目录下，执行
truffle migrate --network  ycc
# truffle会将这三个合约文件编译部署到链上，出现以下提示代表部署成功
Deploying 'ArtMarketplace'
--------------------------
> transaction hash:    0xa1893aa3776589e2aceb1b9ed1a3355778d732dfd77c8593b1a7f2b7452feea5
> contract address:    0x4eB62CdCC3767937108cF88a41BDc9e6DE43Bf27
> block number:        424792
>......省略信息

Summary
=======
> Total deployments:   3
> Final cost:          0.03766312 ETH
```

-运行客户端和服务端
```
# 运行客户端
cd client
yarn
yarn start

# 运行服务端
cd backend
yarn
yarn start
```

3. 操作使用
-  访问前端并通过metamask登录，登录成功后自己的钱包地址会显示在右上角   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market1.png)   
- 点击[Mint your art]发行一个NFT，在弹出的metamask插件中点确认交易   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market2.png)   
- 发行成功后，NFT图片会在首页显示，点详情，再点[sell]按钮，就可将该NFT上架销售。  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market3.png)   
- 其它用户在界面能看到上架的NFT，可进行购买  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market4.png)   

**备注：以太坊的精度是10的18次方，YCC的精度是10的8次方，开发时要注意精度的转换， 处理细节待补充**

#### JAVA-SDK
待补充

#### GO-SDK  
待补充

## 应用和YCC平行链对接注意事项   
由于YCC主链涉及燃料费,同时YCC主链平均每1-3秒一个确认的特性,可能会存在交易的失败,主要有以下两大类情况：  
1. 交易上链了，但交易执行失败（有返回交易hash）：   这类交易通过了mempool（交易缓存池）的合法性检查，但是在合约执行过程中失败了（ 比如转移了错误数量的NFT）。
2. 交易没有上链（没有返回交易hash，rpc接口直接返回出错信息）： 这类交易在mempool的合法性检查中没有通过，包括以下以类错误：  
	- 签名错误（ErrSign）： 签名校验不通过，一般不会遇到，除非人为去改交易内容。  -- 不常见   
	- 交易重复（ErrDupTx）：mempool中发现重复交易，一般不会遇到，除非人为发送重复交易（所谓重复是hash完全一模一样的两笔交易，而不是指业务上数据相同）。 -- 不常见   
	- 手续费不足： 代扣地址下手续费不足会导致交易无法上链，需要保证代扣地址下GAS费充足。  -- 有可能会遇到  
	- 手续费太低（ErrTxFeeTooLow）： 交易设置的手续费比链上要求的手续费低，常见于部署EVM合约或批量发行大量NFT的场合， 需要通过queryEVMGas预估计出一个GAS费，然后在这个基础上再加上0.001作为手续费，这样能保证交易不会上链失败。 -- 有可能遇到，参考用例中手续费设置方式 
	- 交易账户在mempool中存在过多交易（ErrManyTx）： YCC为防止来自于同一个地址的频繁交易，限制每个账户在mempool中的最大交易数量不能超过100， 所以当交易频率很高时，mempool中代扣手续费的交易（都是来自同一个代扣地址）可能会超过100的， 而100笔以后的交易会被丢弃（rpc返回errmanytx的错）从而导致关联的交易也被丢弃。   -- 有可能会遇到   
