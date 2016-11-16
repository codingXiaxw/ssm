package controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import po.Items;
import po.ItemsCustom;
import service.ItemsService;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by codingBoy on 16/11/15.
 */
@Controller
//定义url的根路径，访问时根路径+方法名的url
@RequestMapping("/items")
public class ItemsController {

    //注入service
    @Autowired
    private ItemsService itemsService;

    @RequestMapping("/queryItems")
    public ModelAndView queryItems() throws Exception {
        //调用servie来查询商品列表
        List<ItemsCustom> itemsList=itemsService.findItemsList(null);

        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("itemsList",itemsList);
        //指定逻辑视图名itemsList.jsp
        modelAndView.setViewName("itemsList");

        return modelAndView;
    }

    //商品修改页面提示
    //使用method = RequestMethod.GET来限制使用get方法
//    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
//    public ModelAndView editItems() throws Exception
//    {
//        ModelAndView modelAndView=new ModelAndView();
//
//        //调用service查询商品的信息
//        ItemsCustom itemsCustom=itemsService.findItemsById(1);
//        //将模型数据传到jsp
//        modelAndView.addObject("item",itemsCustom);
//        //指定逻辑视图名
//        modelAndView.setViewName("editItem");
//
//        return modelAndView;
//    }

    //方法返回字符串，字符串就是逻辑视图名，Model作用时将数据填充到request域，在页面显示
    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
    public String editItems(Model model, Integer id) throws Exception
    {

        //将id传到页面
        model.addAttribute("id",id);

        //调用service查询商品的信息
        ItemsCustom itemsCustom=itemsService.findItemsById(id);

        model.addAttribute("itemsCustom",itemsCustom);

        return "editItem";
    }


//    @RequestMapping(value = "/editItems",method = RequestMethod.GET)
//    public void editItems(HttpServletRequest request, HttpServletResponse response,
////                          @RequestParam(value = "item_id",required = false,defaultValue = "1")
//                                  Integer id) throws Exception
//    {
//
//        //调用service查询商品的信息
//        ItemsCustom itemsCustom=itemsService.findItemsById(id);
//
//        request.setAttribute("item",itemsCustom);
//
//        //zhuyi如果使用request转向页面，这里需要指定页面的完整路径
//        request.getRequestDispatcher("/WEB-INF/jsp/editItem.jsp").forward(request,response);
//    }

    //商品提交页面
    //itemsQueryVo是包装类型的pojo
    @RequestMapping("/editItemSubmit")
    public String editItemSubmit(Model model,Integer id,@ModelAttribute(value = "itemsCustom") ItemsCustom itemsCustom) throws Exception
    {
        //进行数据回显
        model.addAttribute("id",id);
//        model.addAttribute("item",itemsCustom);


        itemsService.updateItems(id,itemsCustom);
        //请求转发
//        return "forward:queryItems.action";


        return "editItem";
        //重定向
//        return "redirect:queryItems.action";
    }
//
//    //自定义属性编辑器
//    @InitBinder
//    public void initBinder(WebDataBinder binder) throws  Exception{
//
//        //Date.class必须是与controller方法形参pojo属性一致的date类型，这里是java.util.Date
//        binder.registerCustomEditor(Date.class,new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss"),true));
//
//    }
}
