/**
 * User: sunlong Date: 13-2-2 Time: 上午10:37
 */
var common = {};
common.CONTEXT_PATH = "/hivemeta";
// common.RESPONSE_SEPRATOR = ",";//Controller层返回Response对象msg信息部如果存在多组信息时的分割符号比如return new Response(true,"添加成功,10001")
//
// /**
//  * 异步操作
//  *
//  * @param url
//  */
// common.ajaxOperation = function(url, data, errorId) {
//     data = data || {};
// 	$.post(url, data, function(data) {
// 		if (data.success) {
//             setTimeout(function(){
//                 location.reload();
//             }, 1000);
// 		} else {
//             if(errorId){
//                 common.showError(errorId, data.data);
//             }else{
//                 alert(data.data);
//             }
// 		}
// 	});
// };
//
// /**
//  * 显示错误消息
//  *
//  * @param errorDiv
//  * @param errorMsg
//  */
// common.showSuccess = function(errorDiv, errorMsg) {
//     $(errorDiv).html('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button>' + errorMsg + '</div>').show();
// };
//
// common.showError = function(errorDiv, errorMsg) {
// 	$(errorDiv).html('<div class="alert alert-danger"><button type="button" class="close" data-dismiss="alert">&times;</button>' + errorMsg + '</div>').show();
// };
//
// common.showInfo = function(errorDiv, errorMsg) {
//     $(errorDiv).html('<div class="alert alert-info"><button type="button" class="close" data-dismiss="alert">&times;</button>' + errorMsg + '</div>').show();
// };
//
// common.showWarning = function(errorDiv, errorMsg) {
//     $(errorDiv).html('<div class="alert alert-warning"><button type="button" class="close" data-dismiss="alert">&times;</button>' + errorMsg + '</div>').show();
// };
//
// /**
//  * 禁止输入空格
//  */
// common.keydownFalse = function() {
// 	$('document').on('keydown', 'input', function(event) {
// 		if (event.keyCode == 32) {
// 			return false;
// 		}
// 	});
// };
//
// /**
//  * 数组添加移除元素功能
//  *
//  * @param from
//  * @param to
//  * @return {*}
//  */
// Array.prototype.remove = function(from, to) {
// 	var rest = this.slice((to || from) + 1 || this.length);
// 	this.length = from < 0 ? this.length + from : from;
// 	return this.push.apply(this, rest);
// };
//
// //support indexOf() under IE9
// if (!Array.prototype.indexOf){
//     Array.prototype.indexOf = function(elt /*, from*/){
//         var len = this.length >>> 0;
//         var from = Number(arguments[1]) || 0;
//         from = (from < 0) ? Math.ceil(from) : Math.floor(from);
//         if (from < 0)
//             from += len;
//
//         for (; from < len; from++){
//             if (from in this && this[from] === elt)
//                 return from;
//         }
//         return -1;
//     };
// }
//
// //设置validation默认样式
// $.validator.setDefaults({
//     errorClass: 'alert alert-warning',
//     errorElement: 'div'
// });
//
// $(function(){
//     $("a.dropdown-toggle").mouseenter(function(){
//         $(this).trigger('click');
//     });
//
//     //修正bootstrap modal 垂直不居中情况，水平居中使用css
//     $('body').on('show', '.modal', function(){
//         $(this).css({'margin-top':($(window).height()-$(this).height())/2,'top':'0'});
//     });
//
//     $('th[data-sort]').click(function(){
//         var params = $(this).attr('data-sort').split(',');
//         var sortDir = 'ASC';
//         if(params[3].trim() == 'ASC'){
//             sortDir = 'DESC';
//         }
//         location.href = params[0].trim() + '?sortName=' + params[2].trim() + '&sortDir=' + sortDir + "&" + params[1].trim();
//     });
// });
//
// common.getAbsolutePath = function(){
//     var index = location.pathname.indexOf('/', 1);
//     if(index == -1){
//         return "/";
//     }else{
//         return location.pathname.substr(0, index+1);
//     }
// };