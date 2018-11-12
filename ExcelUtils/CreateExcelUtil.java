/**
 * @Title: CreateExcelUtil.java
 * @Description: ����Excel������ 
 * @version V1.0
 */
public class CreateExcelUtil {

	private String fileName = null; // �ļ���
	private String sheetName = null; // ҳ��
	private ServletOutputStream os = null;
	private WritableWorkbook workbook = null;
	private WritableSheet sheet = null;
	private int sign = 0; // �кű��
	private int sheetNum = 1; // excelҳ�����

	public CreateExcelUtil(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * ����Excelģ��
	 *
	 * @param excelModel
	 * @param response
	 * @throws IOException
	 */
	public void createLoanExcel(HttpServletResponse response) throws IOException {
		String recommentdedName = new String(fileName.getBytes("gbk"), "iso-8859-1");
		String contentType = "application/vnd.ms-excel";
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", "attachment;filename=" + recommentdedName + ".xls");
		// ����������
		os = response.getOutputStream();
		workbook = Workbook.createWorkbook(os);
	}

	/**
	 * �����µ�һҳ
	 *
	 * @param sheetName
	 * @return
	 */
	public WritableSheet createNewSheet(String sheetName) {
		this.sheetName = sheetName;
		sign = 0;
		return sheet = workbook.createSheet(sheetName, 0);
	}

	/**
	 * �����ͷ
	 *
	 * @param excelModel
	 * @throws WriteException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws BusinessException
	 */
	public void setExcelHead(String title, ExcelModel excelModel) throws WriteException, IOException, IllegalAccessException, BusinessException {
		if (StringUtils.isNotBlank(title)) {
			// �����ͷ
			int excelLabelsMapSize = excelModel.getExcelLabels().size();
			sheet.mergeCells(0, sign, excelLabelsMapSize - 1, sign);// ��Ӻϲ���Ԫ�񣬵�һ����������ʼ�У��ڶ�����������ʼ�У���������������ֹ�У����ĸ���������ֹ��
			WritableCellFormat titleFormate = getTitleFormate();
			Label titleLab = new Label(0, sign, title, titleFormate);
			sheet.setRowView(0, 600, false);// ���õ�һ�еĸ߶�
			sheet.addCell(titleLab);
			addSign(excelModel.getExcelLabels()); // ����һ��
		}
	}

	/**
	 * ��Excel��д������
	 *
	 * @param excelModel
	 * @param tableServiceMap
	 * @throws WriteException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws BusinessException
	 */
	public void setExcel(ExcelModel excelModel, Map<String, Object> tableServiceMap) throws WriteException, IllegalAccessException, InstantiationException {
		Map<String, ExcelLabel> excelLabelsMap = excelModel.getExcelLabels(); // ��ȡExcel����ͷ���ּ���ʽ��Ϣ

		if (!excelLabelsMap.isEmpty()) {
			setExcelLabel(excelLabelsMap); // д��ÿ�е�����
			setEntity(excelModel, tableServiceMap, excelLabelsMap, getContextFormate()); // д��ʵ������
		}
	}

	/**
	 * д��ÿ�е�����
	 *
	 * @param excelLabelsMap
	 * @param subTitleFormate
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	private void setExcelLabel(Map<String, ExcelLabel> excelLabelsMap) throws WriteException, RowsExceededException {
		Set<String> excelLabelsSet = excelLabelsMap.keySet();
		for (String key : excelLabelsSet) {
			ExcelLabel e = excelLabelsMap.get(key);
			Label titleLabel = new Label(e.getC(), sign, e.getContName(), getSubTitleFormate());
			sheet.setColumnView(e.getColumnViewArg0(), e.getColumnViewArg1());
			sheet.addCell(titleLabel);
		}
		addSign(excelLabelsMap); // ����һ��
	}

	/**
	 * ��ʵ������д��excel
	 *
	 * @param excelModel
	 * @param tableServiceMap
	 * @param excelLabelsMap
	 * @param contextFormate
	 * @throws WriteException
	 * @throws RowsExceededException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void setEntity(ExcelModel excelModel, Map<String, Object> tableServiceMap, Map<String, ExcelLabel> excelLabelsMap, WritableCellFormat contextFormate) throws WriteException, RowsExceededException, IllegalAccessException, InstantiationException {
		List<?> entityList = excelModel.getEntityList();
		if (entityList == null || entityList.size() <= 0) // ���б�����
			return;
		Set<String> excelLabelsSet = excelLabelsMap.keySet();
		Object entity = entityList.get(0);
		if (entity instanceof Map) { // Map���ʹ���
			for (int i = 0; i < entityList.size(); i++) {
				Object result = entityList.get(i);
				if (null == result) // �ų����ܳ��ֵķǿ�ȫnull�б�
					continue;
				setExcelForMap(tableServiceMap, excelLabelsMap, contextFormate, excelLabelsSet, result);
				addSign(excelLabelsMap); // ����һ��
			}
		} else { // ��Ϊʵ�崦��
			for (int i = 0; i < entityList.size(); i++) {
				Object result = entityList.get(i);
				if (null == result) // �ų����ܳ��ֵķǿ�ȫnull�б�
					continue;
				setExcelForExample(tableServiceMap, excelLabelsMap, contextFormate, result);
				addSign(excelLabelsMap); // ����һ��
			}
		}
	}

	/**
	 * ��ʵ���е����ݲ���Excel
	 *
	 * @param tableServiceMap
	 * @param excelLabelsMap
	 * @param contextFormate
	 * @param result
	 * @throws IllegalAccessException
	 * @throws WriteException
	 * @throws RowsExceededException
	 * @throws InstantiationException
	 */
	private void setExcelForExample(Map<String, Object> tableServiceMap, Map<String, ExcelLabel> excelLabelsMap, WritableCellFormat contextFormate, Object result) throws IllegalAccessException, WriteException, RowsExceededException, InstantiationException {
		Class<? extends Object> resultClazz = result.getClass();
		Type resultSuperClazz = resultClazz.getGenericSuperclass();
		if (null != resultSuperClazz && Object.class != resultSuperClazz) { // ��������
			Object superResult = resultClazz.getSuperclass().newInstance();
			BeanUtils.copyProperties(result, superResult);
			setExcelForExample(tableServiceMap, excelLabelsMap, contextFormate, superResult);
		}
		for (Field field : resultClazz.getDeclaredFields()) {
			field.setAccessible(true);
			ExcelLabel el = excelLabelsMap.get(field.getName());
			if (null == el)
				continue;
			String value = field.get(result) != null ? field.get(result).toString() : ""; // �����ݿⷵ�ؽ������ȡ����Ӧֵ
			if (null != el.getNumberFormat()) {
				formatNumber(el, value); // ��ʽ������
			} else if (null != el.getDataFormat() && Constants.DATE.equals(el.getDataFormat())) {
				formatDate(el, value, contextFormate); // ��ʽ������
			} else {
				value = lookupLabel(tableServiceMap, contextFormate, el, value); // ת��
			}
		}
	}

	/**
	 * ��Map�е����ݲ���Excel
	 *
	 * @param tableServiceMap
	 * @param excelLabelsMap
	 * @param contextFormate
	 * @param result
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	@SuppressWarnings("unchecked")
	private void setExcelForMap(Map<String, Object> tableServiceMap, Map<String, ExcelLabel> excelLabelsMap, WritableCellFormat contextFormate, Set<String> excelLabelsSet, Object result) throws WriteException, RowsExceededException {
		Map<String, Object> resultMap = (Map<String, Object>) result;
		for (String key : excelLabelsSet) {
			ExcelLabel el = excelLabelsMap.get(key);
			Object obj = resultMap.get(key); // �����ݿⷵ�ؽ������ȡ����Ӧֵ
			String value = "";
			if (null != obj)
				value = obj.toString();
			if (null != el.getNumberFormat()) {
				formatNumber(el, value); // ��ʽ������
			} else {
				value = lookupLabel(tableServiceMap, contextFormate, el, value); // ת��
			}
		}
	}

	/**
	 * ����Ƿ���Ҫת��
	 *
	 * @param tableServiceMap
	 * @param contextFormate
	 * @param el
	 * @param value
	 * @return
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	private String lookupLabel(Map<String, Object> tableServiceMap, WritableCellFormat contextFormate, ExcelLabel el, String value) throws WriteException, RowsExceededException {
		if (el.isLookupLabel()) {
			Object serviceObject = tableServiceMap.get(el.getTableService());
			try {
				value = instanceTransService(el, value, serviceObject);
			} catch (Exception e) {
			}
		}
		Label data = new Label(el.getC(), sign, value, contextFormate);
		sheet.addCell(data);
		return value;
	}

	/**
	 * �Ѵ���������д�뵽�������
	 *
	 * @throws IOException
	 */
	public void writeToStream() throws IOException {
		if (workbook != null) {
			workbook.write();
		}
	}

	/**
	 * �ر�����
	 *
	 * @throws IOException
	 * @throws WriteException
	 */
	public void closeConnection() {
		try {
			if (workbook != null) {
				// ���ر������
				workbook.close();
			}
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			workbook = null;
			try {
				if (os != null) {
					os.close();
				}
			} catch (Exception e) {
				// e.printStackTrace();
			} finally {
				os = null;
			}
		}
	}

	/*
	 * ���ݷ�������ʹ�ö�Ӧת�뷽������ת��
	 */
	public String instanceTransService(ExcelLabel el, String value, Object serviceObject) throws BusinessException {
		if (serviceObject instanceof OrgService) {
			Org org = ((OrgService) serviceObject).lookupLabel(value);// ת��
			if (null != org)
				value = org.getOrgname();
		}
		if (serviceObject instanceof CodeTableService) {
			value = ((CodeTableService) serviceObject).lookupLabel(el.getTable(), value);// ת��
		}
		return value;
	}

	public static WritableCellFormat getTitleFormate() throws WriteException {
		WritableFont bold = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);// ������������ͺ�����ʾ,����ΪArial,�ֺŴ�СΪ10,���ú�����ʾ
		WritableCellFormat titleFormate = new WritableCellFormat(bold);// ����һ����Ԫ����ʽ���ƶ���
		titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// ��Ԫ���е�����ˮƽ�������
		titleFormate.setBorder(Border.ALL, BorderLineStyle.THIN);
		titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// ��Ԫ������ݴ�ֱ�������
		return titleFormate;
	}

	public static WritableCellFormat getSubTitleFormate() throws WriteException {
		WritableFont subbold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD); // ������������ͺ�����ʾ,����ΪArial,�ֺŴ�СΪ10,���ú�����ʾ
		WritableCellFormat subTitleFormate = new WritableCellFormat(subbold);// ����һ����Ԫ����ʽ���ƶ���
		subTitleFormate.setAlignment(jxl.format.Alignment.CENTRE);// ��Ԫ���е�����ˮƽ�������
		subTitleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// ��Ԫ������ݴ�ֱ�������
		subTitleFormate.setBorder(Border.ALL, BorderLineStyle.THIN);
		subTitleFormate.setWrap(true);
		return subTitleFormate;
	}

	public static WritableCellFormat getContextFormate() throws WriteException {
		WritableCellFormat contextFormate = new WritableCellFormat();
		contextFormate.setAlignment(jxl.format.Alignment.CENTRE);
		contextFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		contextFormate.setBorder(Border.ALL, BorderLineStyle.THIN);
		return contextFormate;
	}

	public void formatNumber(ExcelLabel el, String value) throws WriteException, RowsExceededException {
		WritableCellFormat cellFormat = new WritableCellFormat(el.getNumberFormat());
		cellFormat.setAlignment(jxl.format.Alignment.CENTRE);
		cellFormat.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		Number number = new Number(el.getC(), sign, Double.parseDouble(value), cellFormat);
		sheet.addCell(number);
	}

	public void formatDate(ExcelLabel el, String value, WritableCellFormat contextFormate) throws WriteException, RowsExceededException {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
		try {
			value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(sdf.parse(value));
		} catch (ParseException e) {
			// ���ת���쳣�ͽ�����ֵд��
			e.printStackTrace();
		}
		Label data = new Label(el.getC(), sign, value, contextFormate);
		sheet.addCell(data);
	}

	public void addSign(Map<String, ExcelLabel> excelLabelsMap) throws RowsExceededException, WriteException { // ����һ��
		sign++;
		if (sign >= Constants.EXCEL_ROWNUM) { // ���д����������excel��ҳ�������������һҳ
			createNewSheet(excelLabelsMap);
		}
	}

	public void addSign(Map<String, ExcelLabel> excelLabelsMap, int addNum) throws RowsExceededException, WriteException { // �����Զ�����
		if ((sign + addNum) > 0) {
			sign = sign + addNum;
			if (sign >= Constants.EXCEL_ROWNUM) { // ���д����������excel��ҳ�������������һҳ
				createNewSheet(excelLabelsMap);
			}
		}
	}

	/**
	 * ���Զ�������һҳ
	 *
	 * @param excelLabelsMap
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	private void createNewSheet(Map<String, ExcelLabel> excelLabelsMap) throws WriteException, RowsExceededException {
		sheetNum++;
		sign = 0;
		sheet = workbook.createSheet(sheetName + sheetNum, sheetNum - 1);
		if (!excelLabelsMap.isEmpty()) {
			setExcelLabel(excelLabelsMap);
		}
	}

	/**
	 * ����Excel�ļ�������ģ��
	 *
	 * @param sheetName
	 *            ҳ��
	 * @param excelModel
	 * @param tableServiceMap
	 * @param response
	 */
	public void simpleCreateExcel(String sheetName, ExcelModel excelModel, Map<String, Object> tableServiceMap, HttpServletResponse response) {
		try {
			// ����Excelģ��
			this.createLoanExcel(response);
			// �����µ�һҳ
			this.createNewSheet(sheetName);
			// ��������
			this.setExcelHead(excelModel.getTitle(), excelModel);
			// д������
			this.setExcel(excelModel, tableServiceMap);
			// д�뵽�������
			this.writeToStream();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// �ر�����
			this.closeConnection();
		}
	}

	/**
	 * ����Excel�ļ���������ģ�壨�����ExcelModel��
	 *
	 * @param sheetName
	 * @param excelModelList
	 * @param tableServiceMap
	 * @param response
	 */
	public void multiCreateExcel(String sheetName, List<ExcelModel> excelModelList, Map<String, Object> tableServiceMap, HttpServletResponse response) {
		try {
			// ����Excelģ��
			this.createLoanExcel(response);
			// �����µ�һҳ
			this.createNewSheet(sheetName);
			// ������������Ҫд��ı������Ƽ����ӦExcelModelʵ��
			Iterator<ExcelModel> ite = excelModelList.iterator();
			while (ite.hasNext()) {
				ExcelModel excelModel = ite.next();
				// ��������
				this.setExcelHead(excelModel.getTitle(), excelModel);
				// д������
				this.setExcel(excelModel, tableServiceMap);
				if (ite.hasNext()) {
					// ����һ��
					this.addSign(null);
				}
			}
			// д�뵽�������
			this.writeToStream();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// �ر�����
			this.closeConnection();
		}
	}

	public int getSign() {
		return sign;
	}

	public WritableSheet getSheet() { // ��ȡ��ǰҳ
		return sheet;
	}
}