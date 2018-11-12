	/**
	 * ********客户查询功能
	 *
	 * @param hirskcstinfVo
	 * @param pageVo
	 * @param model
	 * @return
	 * @throws BusinessException
	 * @throws ParseException
	 */
	@RequestMapping("/hirskcstinfs")
	@ErrorView("hirskcstinf/hirskcstinfList")
	public String queryHirskHirskcstinf(@FormModel(value = "hirskcstinfVo") HirskcstinfVo hirskcstinfVo, PageVo<HirskcstinfVo> pageVo, Model model) throws BusinessException, ParseException {
		PageHelperInterceptor.startPage(pageVo);
		List<String> q_rsktpList = new ArrayList<String>();
		q_rsktpList.add(Constants.RSKTP_HI_TP);
		hirskcstinfVo.setQ_rsktpList(q_rsktpList);
		// hirskcstinfVo.setQ_mvst(Constants.CODE_NO);
		HirskcstinfUtil.checkOrg(hirskcstinfVo);
		this.getHirskcstinfService().findHirskcstinfListByExamplePage(hirskcstinfVo, pageVo);
		PageHelperInterceptor.endPage();
		model.addAttribute("hirskcstinfList", pageVo.getResult());

		return "hirskcstinf/hirskcstinfList";
	}

	/**
	 * 检查用户是否存在********ajax
	 *
	 * @param cstid
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/getHirskcstinfLoanbal")
	public @ResponseBody
	Integer getHirskcstinfLoanbalList(String cstid, Model model) throws BusinessException {
		int loanbalSum = this.getHirskcstinfService().findLoanbalSumByCstIdAndRsktp(cstid, Constants.RSKTP_HI_TP);
		return loanbalSum;
	}

	/**
	 * 移除********客户
	 *
	 * @param hirskcstinfKey
	 * @param model
	 * @return
	 * @throws BusinessException
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/deleteHirskcstinf", method = RequestMethod.POST)
	@ErrorView("hirskcstinf/hirskcstinfList")
	public String deleteHirskcstinfByCstid(HirskcstinfKey hirskcstinfKey, String hdcftmvrsn, HttpServletRequest request, RedirectAttributesModelMap model) throws BusinessException, UnsupportedEncodingException {
		Hirskcstinf hirskcstinf = this.getHirskcstinfService().selectHirskcstinfByPrimaryKey(hirskcstinfKey);
		model.addFlashAttribute(Constants.OPERATE_FLAG, "fault");
		if (Constants.SELF_INTOTP_TP.equals(hirskcstinf.getIntofshn()) && StringUtils.isNotBlank(hdcftmvrsn)) {
			hirskcstinf.setHdcftmvrsn(hdcftmvrsn);
			this.getHirskcstinfService().deleteHirskcstinf(hirskcstinf);
			model.addFlashAttribute(Constants.OPERATE_FLAG, "success");
		}
		String redirectUrl = InternalResourceViewResolver.REDIRECT_URL_PREFIX + request.getSession().getAttribute(Constants.BEFORE_RETURN_URL_KEY);
		return CommonUtil.encodeChar2GBK(redirectUrl);
	}

	/**
	 * 跳转到新增页面
	 *
	 * @param hirskcstinfExp
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	@ErrorView("hirskcstinf/addHirskcstinf")
	public String toAddPage(@FormModel(value = "hirskcstinfVo") HirskcstinfVo hirskcstinfVo, PageVo<HirskcstinfVo> pageVo, Model model) throws BusinessException {
		return "hirskcstinf/addHirskcstinf";
	}

	/**
	 * 新增功能，录入客户号 查询
	 *
	 * @param hirskcstinfExp
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ErrorView("hirskcstinf/addHirskcstinf")
	public String addPage(@FormModel(value = "hirskcstinfVo") HirskcstinfVo hirskcstinfVo, PageVo<HirskcstinfVo> pageVo, Model model) throws BusinessException {
		if (StringUtils.isNotBlank(hirskcstinfVo.getQ_cstid())) {
			PageHelperInterceptor.startPage(pageVo);
			hirskcstinfVo.setQ_rsktp(Constants.RSKTP_HI_TP);
			HirskcstinfUtil.checkOrg(hirskcstinfVo);
			List<Loaninf> loaninfs = this.getHirskcstinfService().findLoaninfByCstidHandwork(hirskcstinfVo, pageVo);
			PageHelperInterceptor.endPage();
			model.addAttribute("loaninfCount", loaninfs.size());
			model.addAttribute("loaninf", pageVo.getResult());
		}
		return "hirskcstinf/addHirskcstinf";
	}

	/**
	 * 检查用户是否已经是********用户 ajax
	 *
	 * @param cstid
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping("/getHirskcstinfCount")
	public @ResponseBody
	Integer getHirskcstinfCount(String cstid, Model model) throws BusinessException {
		int hirskcstinfCount = this.getHirskcstinfService().findCountByCstid(cstid, Constants.RSKTP_HI_TP);
		return hirskcstinfCount;
	}

	/**
	 * 跳转到********原因录入页面
	 *
	 * @param hirskcstinfExp
	 * @param model
	 * @return
	 * @throws BusinessException
	 */
	@RequestMapping(value = "/toreason", method = RequestMethod.POST)
	@Token(saveToken = true)
	public String toReasonPage(HirskcstinfExp hirskcstinfExp, Model model) throws BusinessException {
		return "hirskcstinf/addReason";
	}

	/**
	 * 新增********客户
	 *
	 * @param hirskcstinfExp
	 * @param model
	 * @return
	 * @throws BusinessException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/addHirskcs", method = RequestMethod.POST)
	@ErrorView("hirskcstinf/addReason")
	@Token(removeToken = true)
	public String addHirskcstinf(@FormModel(value = "hirskcstinfExp") HirskcstinfExp hirskcstinfExp, HttpServletRequest request, RedirectAttributesModelMap model) throws BusinessException, IllegalAccessException, InvocationTargetException, UnsupportedEncodingException {
		this.getHirskcstinfService().addHirskcstinfAndHirskloancrcsList(hirskcstinfExp);

		model.addFlashAttribute(Constants.OPERATE_FLAG, "success");
		String redirectUrl = InternalResourceViewResolver.REDIRECT_URL_PREFIX + request.getSession().getAttribute(Constants.BEFORE_RETURN_URL_KEY);
		return CommonUtil.encodeChar2GBK(redirectUrl);
	}
