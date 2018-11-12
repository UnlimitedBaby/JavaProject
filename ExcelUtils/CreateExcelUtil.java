/**
 * @Title: CreateExcelUtil.java
 * @Description: 导出Excel工具类 
 * @version V1.0
 */
public class CreateExcelUtil {

	private String fileName = null; // 文件名
	private String sheetName = null; // 页名
	private ServletOutputStream os = null;
	private WritableWorkbook workbook = null;
	private WritableSheet sheet = null;
	private int sign = 0; // 行号标记
	private int sheetNum = 1; // excel页数标记

	public CreateExcelUtil(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * 生成Excel模板
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
		// 创建工作薄
		os = response.getOutputStream();
		workbook = Workbook.createWorkbook(os);
	}

	/**
	 * 创建新的一页
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
	 * 构造表头
	 *
	 * @param excelModel
	 * @throws WriteException
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws BusinessException
	 */
	public void setExcelHead(String title, ExcelModel excelModel) throws WriteException, IOException, IllegalAccessException, BusinessException {
		if (StringUtils.isNotBlank(title)) {
			// 构造表头
			int excelLabelsMapSize = excelModel.getExcelLabels().size();
			sheet.mergeCells(0, sign, excelLabelsMapSize - 1, sign);// 添加合并单元格，第一个参数是起始列，第二个参数是起始行，第三个参数是终止列，第四个参数是终止行
			WritableCellFormat titleFormate = getTitleFormate();
			Label titleLab = new Label(0, sign, title, titleFormate);
			sheet.setRowView(0, 600, false);// 设置第一行的高度
			sheet.addCell(titleLab);
			addSign(excelModel.getExcelLabels()); // 下移一行
		}
	}

	/**
	 * 向Excel中写入数据
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
		Map<String, ExcelLabel> excelLabelsMap = excelModel.getExcelLabels(); // 获取Excel标题头文字及样式信息

		if (!excelLabelsMap.isEmpty()) {
			setExcelLabel(excelLabelsMap); // 写入每列的列名
			setEntity(excelModel, tableServiceMap, excelLabelsMap, getContextFormate()); // 写入实体内容
		}
	}

	/**
	 * 写入每列的列名
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
		addSign(excelLabelsMap); // 下移一行
	}

	/**
	 * 将实体内容写入excel
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
		if (entityList == null || entityList.size() <= 0) // 空列表不处理
			return;
		Set<String> excelLabelsSet = excelLabelsMap.keySet();
		Object entity = entityList.get(0);
		if (entity instanceof Map) { // Map类型处理
			for (int i = 0; i < entityList.size(); i++) {
				Object result = entityList.get(i);
				if (null == result) // 排除可能出现的非空全null列表
					continue;
				setExcelForMap(tableServiceMap, excelLabelsMap, contextFormate, excelLabelsSet, result);
				addSign(excelLabelsMap); // 下移一行
			}
		} else { // 作为实体处理
			for (int i = 0; i < entityList.size(); i++) {
				Object result = entityList.get(i);
				if (null == result) // 排除可能出现的非空全null列表
					continue;
				setExcelForExample(tableServiceMap, excelLabelsMap, contextFormate, result);
				addSign(excelLabelsMap); // 下移一行
			}
		}
	}

	/**
	 * 将实例中的数据插入Excel
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
		if (null != resultSuperClazz && Object.class != resultSuperClazz) { // 遍历父类
			Object superResult = resultClazz.getSuperclass().newInstance();
			BeanUtils.copyProperties(result, superResult);
			setExcelForExample(tableServiceMap, excelLabelsMap, contextFormate, superResult);
		}
		for (Field field : resultClazz.getDeclaredFields()) {
			field.setAccessible(true);
			ExcelLabel el = excelLabelsMap.get(field.getName());
			if (null == el)
				continue;
			String value = field.get(result) != null ? field.get(result).toString() : ""; // 从数据库返回结果集中取出对应值
			if (null != el.getNumberFormat()) {
				formatNumber(el, value); // 格式化数字
			} else if (null != el.getDataFormat() && Constants.DATE.equals(el.getDataFormat())) {
				formatDate(el, value, contextFormate); // 格式化日期
			} else {
				value = lookupLabel(tableServiceMap, contextFormate, el, value); // 转码
			}
		}
	}

	/**
	 * 将Map中的数据插入Excel
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
			Object obj = resultMap.get(key); // 从数据库返回结果集中取出对应值
			String value = "";
			if (null != obj)
				value = obj.toString();
			if (null != el.getNumberFormat()) {
				formatNumber(el, value); // 格式化数字
			} else {
				value = lookupLabel(tableServiceMap, contextFormate, el, value); // 转码
			}
		}
	}

	/**
	 * 检查是否需要转码
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
	 * 把创建的内容写入到输出流中
	 *
	 * @throws IOException
	 */
	public void writeToStream() throws IOException {
		if (workbook != null) {
			workbook.write();
		}
	}

	/**
	 * 关闭连接
	 *
	 * @throws IOException
	 * @throws WriteException
	 */
	public void closeConnection() {
		try {
			if (workbook != null) {
				// 并关闭输出流
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
	 * 根据方法类型使用对应转码方法进行转码
	 */
	public String instanceTransService(ExcelLabel el, String value, Object serviceObject) throws BusinessException {
		if (serviceObject instanceof OrgService) {
			Org org = ((OrgService) serviceObject).lookupLabel(value);// 转码
			if (null != org)
				value = org.getOrgname();
		}
		if (serviceObject instanceof CodeTableService) {
			value = ((CodeTableService) serviceObject).lookupLabel(el.getTable(), value);// 转码
		}
		return value;
	}

	public static WritableCellFormat getTitleFormate() throws WriteException {
		WritableFont bold = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD);// 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
		WritableCellFormat titleFormate = new WritableCellFormat(bold);// 生成一个单元格样式控制对象
		titleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
		titleFormate.setBorder(Border.ALL, BorderLineStyle.THIN);
		titleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
		return titleFormate;
	}

	public static WritableCellFormat getSubTitleFormate() throws WriteException {
		WritableFont subbold = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD); // 设置字体种类和黑体显示,字体为Arial,字号大小为10,采用黑体显示
		WritableCellFormat subTitleFormate = new WritableCellFormat(subbold);// 生成一个单元格样式控制对象
		subTitleFormate.setAlignment(jxl.format.Alignment.CENTRE);// 单元格中的内容水平方向居中
		subTitleFormate.setVerticalAlignment(jxl.format.VerticalAlignment.CENTRE);// 单元格的内容垂直方向居中
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
			// 如果转换异常就将传入值写出
			e.printStackTrace();
		}
		Label data = new Label(el.getC(), sign, value, contextFormate);
		sheet.addCell(data);
	}

	public void addSign(Map<String, ExcelLabel> excelLabelsMap) throws RowsExceededException, WriteException { // 新增一行
		sign++;
		if (sign >= Constants.EXCEL_ROWNUM) { // 如果写入行数超过excel单页最大行数，新起一页
			createNewSheet(excelLabelsMap);
		}
	}

	public void addSign(Map<String, ExcelLabel> excelLabelsMap, int addNum) throws RowsExceededException, WriteException { // 新增自定义行
		if ((sign + addNum) > 0) {
			sign = sign + addNum;
			if (sign >= Constants.EXCEL_ROWNUM) { // 如果写入行数超过excel单页最大行数，新起一页
				createNewSheet(excelLabelsMap);
			}
		}
	}

	/**
	 * （自动）新起一页
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
	 * 生成Excel文件简单流程模板
	 *
	 * @param sheetName
	 *            页名
	 * @param excelModel
	 * @param tableServiceMap
	 * @param response
	 */
	public void simpleCreateExcel(String sheetName, ExcelModel excelModel, Map<String, Object> tableServiceMap, HttpServletResponse response) {
		try {
			// 创建Excel模板
			this.createLoanExcel(response);
			// 创建新的一页
			this.createNewSheet(sheetName);
			// 创建标题
			this.setExcelHead(excelModel.getTitle(), excelModel);
			// 写入数据
			this.setExcel(excelModel, tableServiceMap);
			// 写入到输出流中
			this.writeToStream();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭连接
			this.closeConnection();
		}
	}

	/**
	 * 生成Excel文件复杂流程模板（含多个ExcelModel）
	 *
	 * @param sheetName
	 * @param excelModelList
	 * @param tableServiceMap
	 * @param response
	 */
	public void multiCreateExcel(String sheetName, List<ExcelModel> excelModelList, Map<String, Object> tableServiceMap, HttpServletResponse response) {
		try {
			// 创建Excel模板
			this.createLoanExcel(response);
			// 创建新的一页
			this.createNewSheet(sheetName);
			// 遍历出所有需要写入的标题名称及其对应ExcelModel实体
			Iterator<ExcelModel> ite = excelModelList.iterator();
			while (ite.hasNext()) {
				ExcelModel excelModel = ite.next();
				// 创建标题
				this.setExcelHead(excelModel.getTitle(), excelModel);
				// 写入数据
				this.setExcel(excelModel, tableServiceMap);
				if (ite.hasNext()) {
					// 下移一行
					this.addSign(null);
				}
			}
			// 写入到输出流中
			this.writeToStream();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭连接
			this.closeConnection();
		}
	}

	public int getSign() {
		return sign;
	}

	public WritableSheet getSheet() { // 获取当前页
		return sheet;
	}
}