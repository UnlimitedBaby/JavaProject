/**
 * Excel导出简单调用示例
 *
 * @param telReturnStatVo
 * @param model
 * @param request
 * @param response
 * @return
 * @throws WriteException
 * @throws IOException
 */
@RequestMapping("/getTelReturnStatExcel")
public String getTelRetStatExcel(@FormModel(value = "telReturnStatVo") TelReturnStatVo telReturnStatVo, Model model, HttpServletRequest request, HttpServletResponse response) throws WriteException, IOException {

	UserAuthContext uac = LoginInfoHolder.getLoginInfo();
	String orgId = telReturnStatVo.getQ_orgId();
	if (StringUtils.isBlank(orgId)) {
		orgId = uac.getUser().getUnit().getEHRNO();
		telReturnStatVo.setQ_orgId(orgId);
	}
	List<Map<String, Object>> telReturnList = this.getInputTelReturnService().getTelReturnStatList(telReturnStatVo);

	ExcelModel excelModel = new ExcelModel();
	// 设置标题名称
	excelModel.setTitle("电话回访统计列表");
	// 设置列名、列号、列宽、转码
	Map<String, ExcelLabel> excelLabels = excelModel.getExcelLabels();
	excelLabels.put("ORGID", new ExcelLabel(0, "机构", 0, 20, "", "OrgService"));
	excelLabels.put("TELRETURNCOUNT", new ExcelLabel(1, "纳入回访笔数", 1, 20));
	excelLabels.put("FINSHCOUNT", new ExcelLabel(2, "已完成回访笔数", 2, 20));
	excelLabels.put("UNFINSHCOUNT", new ExcelLabel(3, "未完成回访笔数", 3, 20));
	excelLabels.put("OVERFCOUNT", new ExcelLabel(4, "超期笔数", 4, 20));
	excelLabels.put("RETCONEXCCOUNT", new ExcelLabel(5, "回访结论异常数", 5, 20));
	excelLabels.put("FINSHRATE", new ExcelLabel(6, "回访完成率", 6, 20, new NumberFormat("0.00%")));
	excelModel.setExcelLabels(excelLabels);
	// 将需要显示的实体列表加入ExcelModel
	excelModel.setEntityList(telReturnList);
	// 添加用于转码的Service
	Map<String, Object> tableServiceMap = new HashMap<String, Object>();
	tableServiceMap.put("OrgService", this.getOrgService());
	// 创建Excel工具类
	CreateExcelUtil createExcelUtil = new CreateExcelUtil("电话回访统计列表");
	// 生成Excel
	createExcelUtil.simpleCreateExcel("电话回访统计列表", excelModel, tableServiceMap, response);
	return null;
}