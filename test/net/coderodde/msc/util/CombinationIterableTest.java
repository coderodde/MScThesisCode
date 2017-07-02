package net.coderodde.msc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CombinationIterableTest {
    
    @Test
    public void testIterator() {
        List<Set<Character>> all = new ArrayList<>();
        
        all.add(new HashSet<>(Arrays.asList('A')));
        all.add(new HashSet<>(Arrays.asList('C')));
        all.add(new HashSet<>(Arrays.asList('G')));
        all.add(new HashSet<>(Arrays.asList('T')));
        
        all.add(new HashSet<>(Arrays.asList('A', 'C', 'G')));
        all.add(new HashSet<>(Arrays.asList('A', 'C', 'T')));
        all.add(new HashSet<>(Arrays.asList('A', 'G', 'T')));
        all.add(new HashSet<>(Arrays.asList('C', 'G', 'T')));
        
        all.add(new HashSet<>(Arrays.asList('A', 'C')));
        all.add(new HashSet<>(Arrays.asList('A', 'G')));
        all.add(new HashSet<>(Arrays.asList('A', 'T')));
        all.add(new HashSet<>(Arrays.asList('C', 'G')));
        all.add(new HashSet<>(Arrays.asList('C', 'T')));
        all.add(new HashSet<>(Arrays.asList('G', 'T')));
        
        all.add(new HashSet<>(Arrays.asList('A', 'C', 'G', 'T')));
        
        Iterator<List<Set<Character>>> iter = 
                new CombinationIterable<>(all).iterator();
        
        int count = 0;
        List<List<Set<Character>>> testList = new ArrayList<>();
        
        while (iter.hasNext()) {
            testList.add(iter.next());
            count++;
        }
        
        System.out.println(count);
        
        // The below test does not fail.
//        for (int i = 0; i < testList.size(); ++i) {
//            for (int j = i + 1; j < testList.size(); ++j) {
//                if (testList.get(i).equals(testList.get(j))) {
//                    fail("Found it!");
//                }
//            }
//        }
    }
}
