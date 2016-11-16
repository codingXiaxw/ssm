package service;

import po.ItemsCustom;
import po.ItemsQueryVo;

import java.util.List;

/**
 * Created by codingBoy on 16/11/15.
 * 商品的service接口
 */

public interface ItemsService {

    //商品的查询列表
    public List<ItemsCustom> findItemsList(ItemsQueryVo itemsQueryVo)
            throws Exception;

    //根据商品id查询商品信息
    public ItemsCustom findItemsById(int id) throws Exception;

    //更新商品信息
    /**
     * 定义service接口，遵循单一职责，将业务参数细化(不要使用包装类型，比如map)
     * @param id 修改商品的id
     * @param itemsCustom 修改商品的信息
     * @throws Exception
     */
    public void updateItems(Integer id,ItemsCustom itemsCustom) throws Exception;

}
