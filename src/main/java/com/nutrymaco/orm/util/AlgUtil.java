package com.nutrymaco.orm.util;

import java.util.ArrayList;
import java.util.List;

public class AlgUtil {

    public static List<List<Object>> getAllCombinations(List<List<Object>> values) {
        return getAllCombinations(values, 0, values.size());
    }

    private static List<List<Object>> getAllCombinations(List<List<Object>> values, int curRow, int length) {
        List<List<Object>> result = new ArrayList<>();
        if (curRow == length) {
            return result;
        }

        List<List<Object>> subproblemResult = getAllCombinations(values, curRow + 1, length);
        int size = subproblemResult.size();

        for (int i = 0; i < values.get(curRow).size(); ++i) {
            if (size > 0) {
                for (List<Object> vals : subproblemResult) {
                    List<Object> currentCombs = new ArrayList<>();
                    currentCombs.add(values.get(curRow).get(i));
                    currentCombs.addAll(vals);
                    result.add(currentCombs);
                }
            } else {
                List<Object> currentCombs = new ArrayList<>();
                currentCombs.add(values.get(curRow).get(i));
                result.add(currentCombs);
            }
        }
        return result;
    }

//    private static List<List<Integer>> cartesianProduct(int[][] arr, int curr_row, int length){
//        List<List<Integer>> res = new ArrayList<>();
//        if(curr_row == length) return res;
//
//        List<List<Integer>> subproblemResult = cartesianProduct(arr,curr_row + 1,length);
//        int size = subproblemResult.size();
//
//        for(int i = 0; i < arr[curr_row].length; ++i){
//            if(size > 0){
//                for (List<Integer> integers : subproblemResult) {
//                    List<Integer> currentCombs = new ArrayList<>();
//                    currentCombs.add(arr[curr_row][i]);
//                    currentCombs.addAll(integers);
//                    res.add(currentCombs);
//                }
//            }else{
//                List<Integer> currentCombs = new ArrayList<>();
//                currentCombs.add(arr[curr_row][i]);
//                res.add(currentCombs);
//            }
//        }
//
//        return res;
//    }
}
