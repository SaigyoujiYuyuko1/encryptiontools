package com.example.asuredelete.Utils;

import it.unisa.dia.gas.jpbc.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
 
/**
 * Created by andyfeng on 2017/12/20.
 */
@Component
public class MerkleTrees {


 
    /**
     * execute merkle_tree and set root.
     */
    public static Element merkle_tree(List<Element> txList) {
 
        List<Element> tempTxList = new ArrayList<Element>();
 
        for (int i = 0; i < txList.size(); i++) {
            int j=0;
            Element leaf = txList.get(i);
            Element index = FuncUtils.getPairing().getG1().newElement(j++);
            Element res= leaf.add(index);
            tempTxList.add(res);
        }
 
        List<Element> newTxList = getNewTxList(tempTxList);
 
        //执行循环，直到只剩下一个hash值
        while (newTxList.size() != 1) {
            newTxList = getNewTxList(newTxList);
        }
 
      return newTxList.get(0);
    }
 
    /**
     * return Node Hash List.
     * @param tempTxList
     * @return
     */
    private static List<Element> getNewTxList(List<Element> tempTxList) {
 
        List<Element> newTxList = new ArrayList<Element>();
        int index = 0;
        while (index < tempTxList.size()) {
            // left
            Element left = tempTxList.get(index);
            index++;
            // right
            Element right =FuncUtils.getZeroFromG1();
            if (index != tempTxList.size()) {
                right = tempTxList.get(index);
            }
            // sha2 hex value
            Element sha2HexValue = hash2Zp(left.duplicate(),right.duplicate());
            newTxList.add(sha2HexValue);
            index++;
 
        }
 
        return newTxList;
    }
 


    /**
     * Get Root
     * @return
     */


    private static Element hash2Zp(Element left,Element right){

        Element res = left.add(right).getImmutable();
        return FuncUtils.hashFromStringToG1(res.toString());

    }

    public static void main(String[] args) {
        MerkleTrees mk=new MerkleTrees();
        Element le = FuncUtils.getZeroFromG1();
        Element ri = FuncUtils.getRandomFromG1();
        long s = System.currentTimeMillis();
        mk.hash2Zp(le, ri);
        long e = System.currentTimeMillis();
        System.out.println(e-s);
    }
}