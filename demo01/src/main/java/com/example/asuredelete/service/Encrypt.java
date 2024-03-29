package com.example.asuredelete.service;

import com.example.asuredelete.Utils.FuncUtils;
import com.example.asuredelete.access.AccessControlParameter;
import com.example.asuredelete.access.parser.ParserUtils;
import com.example.asuredelete.access.tree.AccessTreeEngine;
import com.example.asuredelete.domain.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class Encrypt {
    @Autowired
    private AccessTreeEngine accessTreeEngine;
    @Autowired
    private  BigDFastTransfer bigDFastTransfer;


    private Pairing pairing = FuncUtils.pairing;
   // private BigDecimal zero=BigDecimal.ZERO;

    int MAX = 1024;
    public static int coflen;
    
    public static  BigComplex[] xrays ;
    public static  BigComplex[] aYrays ;
    public static  BigComplex[] ploys ;

    /**
     * 根据访问策略产生访问控制树（叶子节点）
     * @param policy 访问策略
     * @return
     */
    public Map<String, Element> genAccessTree(String policy){

        int[][] accessPolicy ;
        String[] rhos;
        AccessControlParameter accessControlParameter = null;
        try {
            accessPolicy = ParserUtils.GenerateAccessPolicy(policy);
            rhos = ParserUtils.GenerateRhos(policy);
            accessControlParameter= accessTreeEngine.generateAccessControl(accessPolicy, rhos);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //秘密分享到访问树中
        //返回叶子节点属性值-秘密值
        Map<String, Element> stringElementMap = accessTreeEngine.secretSharing(pairing,
                pairing.getZr().newRandomElement(), accessControlParameter);
        return stringElementMap;
    }

    /**
     * 产生根节点孩子节点的参数
     * @param secret rc节点的秘密值（多项式y值）
     * @param num rc节点数量
     * @param policy 与rc节点数量对应的访问策略
     * @return
     */
    public List<RCNode> computeRCNodes(Parameter pp,Element secret,int num,List<String> policy){
        List<RCNode> rcList=new ArrayList<>();
        BigComplex[] ploy=new BigComplex[num];
        ploy[0]=new BigComplex(new BigDecimal(String.valueOf(secret)),BigDecimal.ZERO);
        for (int i = 1; i < num; i++) {
            ploy[i]=new BigComplex(new BigDecimal(FuncUtils.getRandomFromZp().toString()),BigDecimal.ZERO);
        }
        final long start = System.currentTimeMillis();
        BigComplex[] xray = bigDFastTransfer.FFT(ploy, 1);
        final long end = System.currentTimeMillis();
        System.out.println("fft时间:"+(end-start));
//        final long start3 = System.currentTimeMillis();
        BigComplex[] yray = computeY(xray, ploy);
//        final long end3 = System.currentTimeMillis();
//        System.out.println("计算Y值"+(end3-start3));

        //全局变量，删除请求时用到
        coflen=num;
        xrays=xray;
        aYrays=yray;
        ploys=ploy;
        Element g = pp.getG();
        for (int i = 0; i < num; i++) {
            RCNode rc=new RCNode();
            rc.setXray(xray[i]);
            rc.setRoot(xray[i].toString());

            BigInteger tt = new BigInteger(yray[i].toString());

            final Element e = pairing.getZr().newElement(tt);
            Element scrent = e.mul(pairing.getZr().newElement(i));

            rc.setSecret(scrent);
//            rc.setSecret(pairing.getZr().newElement(new BigInteger(yray[i].toString())));
            rc.setGy(g.powZn(rc.getSecret()));
            rc.setStringElementMap(genAccessTree(policy.get(i)));
            final long start2 = System.currentTimeMillis();


            rc.setLeafNodes(computeLeafNode(pp,rc));
            final long end2 = System.currentTimeMillis();
            System.out.println("第"+i+"次计算子树"+(end2-start2));
        }

        return rcList;

    }

    public List<LeafNode> computeLeafNode(Parameter pp,RCNode rc){
        Map<String, Element> map = rc.getStringElementMap();
        Element g = pp.getG();
        List<LeafNode> leafNodes = map.keySet().parallelStream().map(att -> {
            LeafNode leaf = new LeafNode();
            leaf.setCg(g.powZn(map.get(att)));
            leaf.setCy(FuncUtils.hashFromBytesToG1(att.getBytes(StandardCharsets.UTF_8)).powZn(map.get(att)));
            return leaf;
        }).collect(Collectors.toList());
        return leafNodes;
    }

    /**
     * 根据多项式和x坐标计算y坐标(秘密值)
     * @param xray x坐标（单位根）
     * @param ploy 多项式系数集合（有限域中随机数）
     * @return
     */
    public BigComplex[] computeY(BigComplex[] xray,BigComplex[] ploy){
        BigComplex[] yray=new BigComplex[xray.length];
        for (int i = 0; i < xray.length; i++) {
            BigComplex res=new BigComplex(BigDecimal.ZERO,BigDecimal.ZERO);
            for (int j = 0; j < ploy.length; j++) {
                res=BigComplex.Add(res,BigComplex.Mul(ploy[j],xray[i].pow(j)));
            }
            yray[i]=res;
        }
        return yray;
    }

    /**
     * ABE加密算法
     * @param pp
     * @param policy
     * @param num
     */
    @SneakyThrows
    public CT encFile(Parameter pp,PK pk,List<String> policy,int num,String filePath){
        File file=new File(filePath);
        byte[] fileBytes = Files.readAllBytes(file.toPath());

        Element g = pp.getG();
        Element alpha = pp.getAlpha();
        Element s = FuncUtils.getRandomFromZp();
        Element filekey = pairing.pairing(g, g).powZn(alpha.mul(s));
        long start=System.currentTimeMillis();
        Element cipher = filekey.mul(pairing.getGT().newElementFromBytes(fileBytes));
        long end=System.currentTimeMillis();
        System.out.println("加密文件："+(end-start));

        Element c = pk.getH().powZn(s);
        long start1=System.currentTimeMillis();
        List<RCNode> rcNodes = computeRCNodes(pp,alpha, num ,policy);
        long end1=System.currentTimeMillis();
        System.out.println("计算访问控制树："+(end1-start1));
        CT ct=new CT();
        ct.setCipher(cipher);
        ct.setC(c);
        ct.setRcNodesList(rcNodes);

        return ct;
    }


    public static void main(String[] args) {
     String s="123214.123";
        final String[] split = s.split("\\.");
        System.out.println(split.length);
        System.out.println(split[0]);


    }
}
