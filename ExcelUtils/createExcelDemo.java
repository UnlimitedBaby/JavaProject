/**
 * Excel�����򵥵���ʾ��
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
	// ���ñ�������
	excelModel.setTitle("�绰�ط�ͳ���б�");
	// �����������кš��п�ת��
	Map<String, ExcelLabel> excelLabels = excelModel.getExcelLabels();
	excelLabels.put("ORGID", new ExcelLabel(0, "����", 0, 20, "", "OrgService"));
	excelLabels.put("TELRETURNCOUNT", new ExcelLabel(1, "����طñ���", 1, 20));
	excelLabels.put("FINSHCOUNT", new ExcelLabel(2, "����ɻطñ���", 2, 20));
	excelLabels.put("UNFINSHCOUNT", new ExcelLabel(3, "δ��ɻطñ���", 3, 20));
	excelLabels.put("OVERFCOUNT", new ExcelLabel(4, "���ڱ���", 4, 20));
	excelLabels.put("RETCONEXCCOUNT", new ExcelLabel(5, "�طý����쳣��", 5, 20));
	excelLabels.put("FINSHRATE", new ExcelLabel(6, "�ط������", 6, 20, new NumberFormat("0.00%")));
	excelModel.setExcelLabels(excelLabels);
	// ����Ҫ��ʾ��ʵ���б����ExcelModel
	excelModel.setEntityList(telReturnList);
	// �������ת���Service
	Map<String, Object> tableServiceMap = new HashMap<String, Object>();
	tableServiceMap.put("OrgService", this.getOrgService());
	// ����Excel������
	CreateExcelUtil createExcelUtil = new CreateExcelUtil("�绰�ط�ͳ���б�");
	// ����Excel
	createExcelUtil.simpleCreateExcel("�绰�ط�ͳ���б�", excelModel, tableServiceMap, response);
	return null;
}