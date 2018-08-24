
var value;

function child(d) {
    value = null;
    if (d != null) {
        value = d;
    }
}

function tableReload() {
    window.parent.layui.table.reload('idTest', {
        page: {
            curr: 1
        }
    });
}
setTimeout(function() {
layui.use(['table','upload','form'], function(){
    var table = layui.table,
        upload = layui.upload,
        upload1 = layui.upload,
        upload2 = layui.upload,
        form = layui.form;
    var $ = layui.$;


    var  frameindex= parent.layer.getFrameIndex(window.name);//当此页面被其他页面弹窗时，获取此页面元素

    var path ='/book/addBookUpload';
    console.log(value);

    if(value!=""&&value.id!=undefined){
        var indexs= layer.load(1, {
            shade: [0.1,'#fff'] //0.1透明度的白色背景
        });

            $('#imgs').attr('src', value.img); //图片链接（base64
            form.val('newsForm',{
                "id":value.id,
                "deletImg":value.img,
                "deletfileZIP":value.url,
                "title":value.title,
                "type":value.type,
                "bookType":value.bookType,
                "des":value.des
            });
            path ='/book/updateBook';
            $("#btnform").removeClass("layui-hide");//没有上传文件就监听并显示这个按钮
            $("#btn").addClass("layui-hide");//同时隐藏这个按钮


            var zips = value.url.split('/');
            $('#ziptext').html(zips[zips.length-1]);
            layer.close(indexs);

    }

    // $.ajax({
    //     url: "/worksType/pager",
    //     data: {"page":1,"limit":100},
    //     type: "POST",
    //     dataType: "json",
    //     success: function (response) {
    //
    //     },error:function (response) {
    //         alert("接口请求异常")
    //     }
    // });
    var fileZIP =null;

    $("#btnform").click(function () {//当upload.render没有被使用的时候就用这个提交表单数据

        var formdata1=new FormData();// 创建form对象
        formdata1.append('id',$("#ids").val());// 通过append向form对象添加数据,可以通过append继续添加数据
        formdata1.append('deletImg',$("#deletImg").val());
        formdata1.append('deletfileZIP',value.url!=undefined?value.url:null);
        formdata1.append('title',$("#title").val());
        formdata1.append('type',$("#type").val());
        formdata1.append('bookType',$("#bookType").val());
        formdata1.append('des',$("#des").val());

        if(fileZIP!=null){
            formdata1.append('fileZIP',fileZIP,fileZIP.name);
        }
        $("#btnform").addClass("layui-hide");//提交后就暂时隐藏按钮
        var config = {
            headers: {'Content-Type': 'multipart/form-data'}
        };
        axios.post(path, formdata1,config).then(function (response) {
            tableReload();
            parent.layer.close(frameindex);//此页面被其他页面iframe弹窗时，调用此方法进行关闭
            parent.layer.msg(response.data.message, {
                time: 1000, icon:response.data.code==0?6:5
            });
        }).catch(function (error) {
            layer.msg(error);
        });
    });



    upload.render({
           elem: '#uploadImg'
           ,url: path
           ,auto: false
           ,size: 5120
           ,title: '只能上传jpg,png,jpeg后缀的文件'
           ,field:'fileImg' //后台接收默认字段名
           //,multiple: true
           ,bindAction: '#btn'
           ,acceptMime: 'image/jpg, image/png,image/jpeg'
           ,ext: 'jpg|png|jpeg'
            ,choose: function(obj){ //obj参数包含的信息，跟 choose回调完全一致，可参见上文。
                //将每次选择的文件追加到文件队列
            var files = obj.pushFile();
                //预读本地文件，如果是多文件，则会遍历。(不支持ie8/9)
                obj.preview(function(index, file, result){
                    $('#imgs').attr('src', result); //图片链接（base64
                    $("#btn").removeClass("layui-hide");//上传了文件就监听并显示这个按钮
                    $("#btnform").addClass("layui-hide");//同时隐藏这个按钮

                });
            }
            ,before: function(obj){
                //这里是把数据跟图片一起添加到后台
                this.data = {"id":$("#ids").val()
                    ,"deletImg":$("#deletImg").val()
                    ,"deletfileZIP":value.url!=undefined?value.url:null
                    ,"title":$("#title").val(),
                    "type":$("#type").val(),
                    "bookType":$("#bookType").val(),
                    "des":$("#des").val(),
                    "fileZIP":fileZIP
                };
            $("#btn").addClass("layui-hide");//提交后就暂时隐藏按钮
             },done: function(res, indexs, upload){ //上传后的回调
                tableReload();
                parent.layer.close(frameindex);//此页面被其他页面iframe弹窗时，调用此方法进行关闭
                parent.layer.msg(res.message, {
                    time: 1000, icon:res.code==0?6:5
                });
            }
            ,error: function(index, upload){
            layer.msg("上传失败")
           }
       });


    upload2.render({
        elem: '#worksAddress'
        ,url: path
        ,auto: false
        ,size: 71200
        ,title: '只能上传jpg,png,jpeg后缀的文件'
        ,field:'fileZIP' //后台接收默认字段名
        ,accept: 'file '
        ,acceptMime:  '.zip,.rar'
        ,exts: 'zip|rar'
        ,choose: function(obj){ //obj参数包含的信息，跟 choose回调完全一致，可参见上文。
            //将每次选择的文件追加到文件队列

            //预读本地文件，如果是多文件，则会遍历。(不支持ie8/9)
            obj.preview(function(index, file, result){
                // $("#ziptext").html(file.name)
                $('#ziptext').html(file.name);
                fileZIP=file;

            });
        }
        ,done: function(res){
            //如果上传失败
            if(res.code > 0){
                return layer.msg('上传失败');
            }
            //上传成功
        }
        ,error: function(index, upload){
            layer.msg("上传失败")
        }
    });

});
},200);