## һ. ͨ��remix��metamask��YCCƽ�����ϲ������NFT��Լ  
1.  �������װmetamask����������˻������YCCƽ������rpc����  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/metamask.png)    
����RPC URL:  �˴��ĳ��û��Լ�YCCƽ������rpc����    
��ID: ʹ��YCCע��Ĺ̶�ֵ 3999    

2. �˻��³�һЩYCC����������֧����TODO: ��ȡYCC��ʽ��

3. ���벿���Լ
- **׼�����Ժ�Լ**�����Բο� [[ERC1155��Լ]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC1155ByManager.sol)     
- **׼����Լ������**��������remix IDE:  https://remix.ethereum.org/   
- **�½������ļ�**����remix��workspace�£��½�һ��ERC1155.sol�ļ������������ο���Լ���������ȥ��  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix1.png)   
- **�����Լ**��ѡ�õı������汾Ҫ���ڵ��ں�Լ��Ҫ��İ汾
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix2.png)   
- **metamask����remix**������ѡ��[Injected Web3] -- ����metamask�������ʾ�ʲ��������Ӵ���վ -- ѡ�����ӣ� ���ӳɹ���metemask�����õ�chainID���û��˻���ַ������remix��������ʾ��  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix3.png)   
- **�����Լ��YCCƽ������**��ѡ��[deploy]��ť -- ����matemask�������ʾ�����Լ�����GAS�� -- ȷ���Լ��˻���ȼ�ϳ��� -- ���ȷ��-- �ɹ�����ڿ���̨��ӡ��Լ������Ϣ  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix4.png)   
�������Deployed Contracts:  ����õĺ�Լ��ַ  
status: ��Լ����Ľ��  
transaction hash: ��Լ����ɹ����ص�hashֵ  
from: �����˵ĵ�ַ  
gas: �����Լ��Ҫ��gas  
- **���ú�Լ**�����[Deployed Contracts]����������ͷ���ٵ��[mint]�������ϵ�������ͷ������mint��������Ĳ���(ע����������Ҫ��solidity��Լ�ж���ı���һ��)   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix5.png)   
- **��ѯ���**�����[Deployed Contracts]����������ͷ���ٵ��[balanceof]�������ϵ�������ͷ������balanceof��������Ĳ���,���[call]��ť��ѯ  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/remix6.png)   

##��. ͨ��truffle��metamask��YCCƽ�����ϲ���Ӧ��  
��������̫���ϵ�һ��Ӧ�ã�NFT-Marketplace��Ϊ����˵����ΰ���������ֲ��YCCƽ�����ϡ�  
NFT-Marketplace��Դ��ַ��https://github.com/BravoNatalie/NFT-Marketplace�� ʵ����һ��Demo���ʵ�ȥ���Ļ�NFT�����г������Է���Ҳ��������NFT�� 

1. ����׼��
��Ʒ�Ŀͻ���+����ˣ�������Win10������, �����ϵ�visual studio�汾���ڵ��� 2015�� 
������(YCC����+ƽ����������linux��������)  
python3  
Git����  
Node  
Yarn��npm  
Truffle  
metamask�������� 

2. ��������
- ͨ��git bash��������NFT�г���Դ����  
```
git clone https://github.com/BravoNatalie/NFT-Marketplace.git  
```

- ͨ��yarn���װ��������֤����û�����ƣ������Щ��������ʧ�ܣ�������Ҫ��ѧ������ 
```
# ����Ŀ¼
cd NFT-Marketplace  
# ִ��yarn����
yarn  
```

- �޸�truffle�����ļ�
```
# ��NFT-MarketplaceĿ¼�£��޸�truffle-config.js�ļ���
# 1. ���ļ�ͷ����const����
const HDWalletProvider = require('@truffle/hdwallet-provider');
# 2. ��������YCCƽ�������������
    ycc: {
      network_id: "3999",       // YCC  network_id�� ��ӦYCC��chainid���̶���д3999
      from: "0xc05109180ac5298e3a9b7d7e70abf98ffb986d22",  // �û���ַ����Ҫ��֤��ַ����ȼ��
      provider: function() {
        return new HDWalletProvider(mnemonic , "http://121.52.224.92:8546/"); // mnemonic��Ǯ�����Ǵ�; url: YCCƽ������Ӧ��rpc��ַ
      }
    },
```

- ����truffle���ߣ���./contracts/Ŀ¼�µ�������Լ��������
```
# �޸�PATH������������node_modules/.bin�ӵ�PATH��
export PATH=$(pwd)/node_modules/.bin:$PATH
# ɾ��֮ǰ�Ѿ�����õ��ļ�
cd client\src\contracts
rm -rf *
# �ٻص�NFT-MarketplaceĿ¼�£�ִ��
truffle migrate --network  ycc
# truffle�Ὣ��������Լ�ļ����벿�����ϣ�����������ʾ������ɹ�
Deploying 'ArtMarketplace'
--------------------------
> transaction hash:    0xa1893aa3776589e2aceb1b9ed1a3355778d732dfd77c8593b1a7f2b7452feea5
> contract address:    0x4eB62CdCC3767937108cF88a41BDc9e6DE43Bf27
> block number:        424792
>......ʡ����Ϣ

Summary
=======
> Total deployments:   3
> Final cost:          0.03766312 ETH
```

-���пͻ��˺ͷ����
```
# ���пͻ���
cd client
yarn
yarn start

# ���з����
cd backend
yarn
yarn start
```

3. ����ʹ��
-  ����ǰ�˲�ͨ��metamask��¼����¼�ɹ����Լ���Ǯ����ַ����ʾ�����Ͻ�   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market1.png)   
- ���[Mint your art]����һ��NFT���ڵ�����metamask����е�ȷ�Ͻ���   
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market2.png)   
- ���гɹ���NFTͼƬ������ҳ��ʾ�������飬�ٵ�[sell]��ť���Ϳɽ���NFT�ϼ����ۡ�  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market3.png)   
- �����û��ڽ����ܿ����ϼܵ�NFT���ɽ��й���  
![Image text](https://github.com/andyYuanFZM/yccDemo/blob/master/src/test/resources/market4.png)   

**��ע����̫���ľ�����10��18�η���YCC�ľ�����10��8�η�������ʱҪע�⾫�ȵ�ת���� ����ϸ�ڴ�����**