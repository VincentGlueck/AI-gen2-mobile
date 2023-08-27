package org.ww.ai.rds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ww.ai.rds.entity.RenderResultLightWeight;

import java.util.ArrayList;
import java.util.List;

public class PagingCacheTest {

    private PagingCache mPagingCache;
    @Before
    public void setup() {
        mPagingCache = new PagingCache(4);
    }

    @Test
    public void testRemove() {
        createTestEntries(4);
        int avail = mPagingCache.getAvailableCapacity();
        assertEquals(0, avail);
        List<RenderResultLightWeight> list = getRenderResultLightWeights(1, 8000L);
        mPagingCache.addAll(list);
        assertEquals(0, avail);
        List<RenderResultLightWeight> lightWeights = mPagingCache.getAllEntries();
        assertEquals(4, lightWeights.size());
        assertEquals(Long.valueOf(8000L), lightWeights.get(3).createdTime);
    }

    @Test
    public void testHasId() {
        createTestEntries(2);
        assertTrue(mPagingCache.hasId(1));
        assertFalse(mPagingCache.hasId(Integer.MAX_VALUE));
    }

    private void createTestEntries(int count) {
        long time = 1000;
        for(int n=0; n<count; n++) {
            RenderResultLightWeight lw = new RenderResultLightWeight();
            lw.uid = n + 1;
            lw.createdTime = time;
            mPagingCache.addAll(List.of(lw), time);
            time += 1000;
        }
    }

    private List<RenderResultLightWeight> getRenderResultLightWeights(int count, long... startTime) {
        List<RenderResultLightWeight> result = new ArrayList<>();
        long time = startTime.length > 0 ? startTime[0] : System.currentTimeMillis();
        for(int n=0; n<count; n++) {
            RenderResultLightWeight lw = new RenderResultLightWeight();
            lw.uid = n + 1;
            lw.createdTime = time;
            result.add(lw);
            time += 1000;
        }
        return result;
    }


}