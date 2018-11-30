```javascript
Easyui Datagrid的Rownumber行号显示问题:
Datagrid中当你的行数据超过9999时，第一列的行号rownumber将会因为表格内容过长而导致无法显示全部数字，这一点Easyui无法做到自适应，所以需要进行修改，这里扩展一个方法就行了。
第一步:找到jquery.easyui.min.js的文件;
第二步:在源码中搜索$.extend($.fn.datagrid.methods, {
第三步:在该扩展中最后添加如下方法:
,fixRownumber: function (jq) {
	        return jq.each(function () {
	            var panel = $(this).datagrid("getPanel");
	            //获取最后一行的number容器,并拷贝一份
	            var clone = $(".datagrid-cell-rownumber", panel).last().clone();
	            //由于在某些浏览器里面,是不支持获取隐藏元素的宽度,所以取巧一下
	            clone.css({
	                "position" : "absolute",
	                left : -1000
	            }).appendTo("body");
	            var width = clone.width("auto").width();
	            //默认宽度是25,所以只有大于25的时候才进行fix
	            if (width > 25) {
	                //多加5个像素,保持一点边距
	                $(".datagrid-header-rownumber,.datagrid-cell-rownumber", panel).width(width + 5);
	                //修改了宽度之后,需要对容器进行重新计算,所以调用resize
	                $(this).datagrid("resize");
	                //一些清理工作
	                clone.remove();
	                clone = null;
	            } else {
	                //还原成默认状态
	                $(".datagrid-header-rownumber,.datagrid-cell-rownumber", panel).removeAttr("style");
	            }
	        });
	    }
第四步:在table中的添加onLoadSuccess,具体如下:
<table id="dg1" title="Client Side Pagination" style="width:700px;height:300px" data-options="
                rownumbers:true,
                singleSelect:true,
                autoRowHeight:false,
                pagination:true,
                fitColumns:true,
                pageSize:10,
                onLoadSuccess:function(){
                	$(this).datagrid('fixRownumber');
                }">
    </table>
```

