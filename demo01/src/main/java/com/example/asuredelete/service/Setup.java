package com.example.asuredelete.service;

import com.example.asuredelete.Utils.FuncUtils;
import com.example.asuredelete.aop.EXCTime;
import com.example.asuredelete.domain.MSK;
import com.example.asuredelete.domain.PK;
import com.example.asuredelete.domain.Parameter;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Service;

@Service
public class Setup {
   private Pairing pair = FuncUtils.getPairing();

    public Parameter setupPP(){
       return new Parameter(
               pair.getG1(),
                pair.getZr(),
                FuncUtils.getRandomFromG1(),
                FuncUtils.getRandomFromZp(),
                FuncUtils.getRandomFromZp()

        );

    }

    public PK setupPK(Parameter pp){
        PK pk=new PK();
        pk.setG1(pp.getG1());
        pk.setG(pp.getG());
        pk.setH(pp.getG().powZn(pp.getAlpha()));
        pk.setF(pp.getG().powZn(FuncUtils.getOneFromZp().div(pp.getBeta())));
        pk.setEGGA(pair.pairing(pp.getG(),pp.getG()).powZn(pp.getAlpha()));
        return pk;
    }
    public MSK setupMSK(Parameter pp){
        Element sk =  pp.getG().powZn(pp.getAlpha());
        return new MSK(pp.getBeta(),sk);
    }
}
