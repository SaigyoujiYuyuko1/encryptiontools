package com.example.demo02;

import com.example.Demo01ApplicationTests;
import com.example.Xue.Service.XueScheme;
import com.example.asuredelete.Utils.FuncUtils;
import com.example.asuredelete.Utils.SizeUtils;
import com.example.asuredelete.aop.EXCTime;
import com.example.asuredelete.domain.*;
import com.example.asuredelete.service.*;
import it.unisa.dia.gas.jpbc.Element;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
@Slf4j

public class DeleteTest extends Demo01ApplicationTests {
    @Autowired
    private Encrypt encrypt;
    @Autowired
    private DeleteImpl delete;
    @Autowired
    private DeleteRequest deleteRequest;
    @Autowired
    private Setup setup;
    @Autowired
    private ProofCheck proofCheck;
    @Autowired
    private XueScheme xueScheme;

    String access_policy_example_1 = "0 and 1 and (2 or 3)";
    String access_policy_example_2 = "((0 and 1 and 2) and (3 or 4 or 5) and (6 and 7 and (8 or 9 or 10 or 11)))";
    String access_policy_example_3 = "(0 and 1 and 2) and (3 or 4 or 5)";
    String access_policy_example_4 = "(0 and 1 or 2) and (3 or 4 or 5)";
    String access_policy_example_5 = "(0 and 1 and 2) and (3 and 4 or 5)";
    String filePath="D:\\Desktop\\琐碎\\ab.pdf";

    @Test
    @EXCTime
    public void deleteTest(){
        int num=5;
        int att=5;
        long ss = System.currentTimeMillis();
        List<String> policy =new ArrayList<>();
        for (int i = 0; i < num; i++) {
            policy.add(access_policy_example_5);
        }


        try{
            long start = System.currentTimeMillis();
            Parameter pp = setup.setupPP();

            MSK msk = setup.setupMSK(pp);
            PK pk = setup.setupPK(pp);
            CT ct = encrypt.encFile(pp,pk, policy, num,filePath);
            DR dr = deleteRequest.delReq(pp, msk, pk, num);
            List<Element> list = delete.delImpl(pp, ct, dr);
            Element proof = delete.proofGen(pp, list);

            Boolean judge = proofCheck.verifyProof(pp, proof);

            long end = System.currentTimeMillis();
            System.out.println(end-start);
            List<Element> res=new ArrayList<>();
                Element g = FuncUtils.getRandomFromG1();
                Element a = FuncUtils.getRandomFromZp();
            for (int i = 0; i < att; i++) {
                res.add(g.powZn(a));
                res.add(FuncUtils.hashFromStringToG1(i+"").powZn(a));
            }

        System.out.println("CSP的存储开销："+ SizeUtils.getSize(ct.toString().getBytes().length+res.toString().getBytes().length));
        }catch (Exception e){log.info("",e);}
        long ee = System.currentTimeMillis();
        log.info("我的总共时间：{}",ee-ss);


    }
}
