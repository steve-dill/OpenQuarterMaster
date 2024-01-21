<?php

namespace Ebprod\OqmCoreDepot\pageBuilders;

class MainPageBuilder {
	
	protected static function getNav(){
		$activePage = NavLink::cases();
		
		return '
<nav class="navbar navbar-expand-lg bg-light top-nav mb-2" data-bs-theme="light" id="top-nav">
	<div class="container-fluid">
		<a class="navbar-brand p-0" href="/overview">
			<img src="/res/media/logo.svg" alt=""  id="topLogo">
		</a>
		<button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarColor03"
				aria-controls="navbarColor03" aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>
		<div class="collapse navbar-collapse" id="navbarColor03">
			<ul class="navbar-nav me-auto">
				<li class="nav-item">
					<a class="nav-link active" href="/overview">
						{#icons/pageIcon page=\'overview\'}{/icons/pageIcon} Overview
							{#if page is \'overview\'}
								<span class="visually-hidden">(current)</span>
						{/if}
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link {#if page is \'storage\'}active{/if}" href="/storage">
						{#icons/pageIcon page=\'storage\'}{/icons/pageIcon} Storage
							{#if page is \'storage\'}
								<span class="visually-hidden">(current)</span>
						{/if}
					</a>
				</li>
				<li class="nav-item">
					<a class="nav-link pe-0 {#if page is \'items\'}active{/if}" href="/items">
						{#icons/pageIcon page=\'items\'}{/icons/pageIcon} Items
							{#if page is \'items\'}
								<span class="visually-hidden">(current)</span>
						{/if}
					</a>
				</li>

				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle ps-0 {#if page is \'itemLists\' || page is \'categories\' || page is \'itemCheckout\' }active{/if}" data-bs-toggle="dropdown" href="#" role="button"
							aria-haspopup="true" aria-expanded="false">&nbsp;</a>
					<div class="dropdown-menu">
						<!-- Not ready yet
							<a class="dropdown-item {#if page is \'itemLists\'}active{/if}" href="/itemLists">
							{#icons/pageIcon page=\'itemLists\'}{/icons/pageIcon} Item Lists
								{#if page is \'itemLists\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						<a class="dropdown-item" href="/itemLists#add">
							{#icons/add}{/icons/add} New Item List
								</a>
							-->
						<a class="dropdown-item {#if page is \'categories\'}active{/if}" href="/categories">
							{#icons/categories}{/icons/categories} Categories
								{#if page is \'categories\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						{#if userInfo.getRoles().contains(\'inventoryEdit\')}
							<a class="dropdown-item {#if page is \'itemCheckout\'}active{/if}" href="/itemCheckout">
							{#icons/checkinout}{/icons/checkinout} Checkouts
								{#if page is \'itemCheckout\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						{/if}
					</div>
				</li>
				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle {#if page is \'images\' || page is \'codes\' || page is \'files\' || page is \'help\' }active{/if}" data-bs-toggle="dropdown" href="#" role="button"
							aria-haspopup="true" aria-expanded="false">
						{#icons/icon icon=\'infinity\'}{/icons/icon} Other</a>
							<div class="dropdown-menu">
						<a class="dropdown-item {#if page is \'images\'}active{/if}" href="/images">
							{#icons/pageIcon page=\'images\'}{/icons/pageIcon} Images
								{#if page is \'images\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						<a class="dropdown-item {#if page is \'files\'}active{/if}" href="/files">
							{#icons/pageIcon page=\'files\'}{/icons/pageIcon} Files
								{#if page is \'files\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						<a class="dropdown-item {#if page is \'codes\'}active{/if}" href="/codes">
							{#icons/pageIcon page=\'codes\'}{/icons/pageIcon} QR &amp; Bar Code Generator
								{#if page is \'codes\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
						<a class="dropdown-item {#if page is \'help\'}active{/if}" href="/help">
							{#icons/pageIcon page=\'help\'}{/icons/pageIcon} Help & User Guide
								{#if page is \'help\'}
									<span class="visually-hidden">(current)</span>
							{/if}
						</a>
					</div>
				</li>
			</ul>

			<form class="d-flex me-auto" method="get" action="/items" id="navSearchForm">
				<div class="input-group">
					<input class="form-control" id="navSearchInput" type="text" placeholder="Search" name="name">
					<select class="form-select" id="navSearchTypeSelect" aria-label="Navbar Quick Search search type" style="max-width: 140px;">
						<option data-action="/items" data-field="name" selected>Items</option>
						<option data-action="/storage" data-field="label">Storage Blocks</option>
					</select>
					<button class="btn btn-outline-dark" type="submit">{#icons/search}{/icons/search} Search</button>
									</div>
			</form>
			<ul class="navbar-nav mb-auto">
				{#if config:[\'runningInfo.depotUri\'] != " "}
					<li class="nav-item">
					<a class="nav-link" href="{config:[\'runningInfo.depotUri\']}">
						{#icons/icon icon=\'box-arrow-in-up-left\'}{/icons/icon} Back to Depot
							</a>
				</li>
				{/if}
				<li class="nav-item dropdown">
					<a class="nav-link dropdown-toggle" data-bs-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false">
						{#icons/user}{/icons/user}
							<span id="userNameDisplay">{userInfo.getName()}</span></a>
					<div class="dropdown-menu">
						<a class="dropdown-item" href="/you" id="youLink">
							{#icons/you}{/icons/you} You
								</a>
						<a class="dropdown-item" href="{config:[\'service.auth.userSettingsUrl\']}" id="youEditLink" target="_blank">
							{#icons/edit}{/icons/edit} Account Settings
								</a>
					{#if userInfo.getRoles().contains(\'userAdmin\') || userInfo.getRoles().contains(\'inventoryAdmin\')}
						<div class="dropdown-divider"></div>
					{/if}
					{#if userInfo.getRoles().contains(\'userAdmin\')}
						<a class="dropdown-item" href="/userAdmin" id="userAdminLink">
						{#icons/userAdmin}{/icons/userAdmin} User Administration
							</a>
					{/if}
					{#if userInfo.getRoles().contains(\'inventoryAdmin\')}
						<a class="dropdown-item" href="/inventoryAdmin" id="inventoryAdminLink">
						{#icons/inventoryAdmin}{/icons/inventoryAdmin} Inventory Admin
							</a>
					{/if}
					<div class="dropdown-divider"></div>
						<a class="dropdown-item" href="{config:[\'quarkus.oidc.logout.path\']}" id="logoutButton">
							{#icons/icon icon=\'door-closed\'}{/icons/icon} Logout
								</a>
					</div>
				</li>
			</ul>
		</div>
	</div>
	{#else if navbar == "toLogin"}
		<div class="container-fluid">
		<a class="navbar-brand" href="/">
			<img src="/media/logo.svg" alt="" height="40" width="97" id="topLogo">
		</a>
		<button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarColor03"
			aria-controls="navbarColor03" aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>

		<div class="collapse navbar-collapse" id="navbarColor03">
			<ul class="navbar-nav me-auto">
				{#if config:[\'runningInfo.depotUri\'] != " "}
					<li class="nav-item">
					<a class="nav-link" href="{config:[\'runningInfo.depotUri\']}">
						{#icons/icon icon=\'box-arrow-in-up-left\'}{/icons/icon} Back to Depot
							</a>
				</li>
				{/if}
				<li class="nav-item">
					<a class="nav-link" href="/">
						{#icons/icon icon=\'door-open\'}{/icons/icon} Login
							</a>
				</li>
			</ul>

		</div>
	</div>
	{/if}
</nav>

';
	}

	public static function getPageStart(
		string $pageStyle = ""
	):string {
		$title = "placeholder";
		$styleSheets = "";
		return '
<!doctype html>
<html lang="en">
<head>
	<!-- Required meta tags -->
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

	<link rel="shortcut icon" href="/favicon.ico"/>
	<title>'.$title.' - OQM Base Station</title>

	<!-- CSS -->
	<link href="/lib/bootstrap/5.3.2/yeti-bootswatch.min.css" rel="stylesheet">
	<link rel="stylesheet" href="/lib/bootstrap-icons/1.11.3/bootstrap-icons.min.css">
	<link rel="stylesheet" href="/lib/spin.js/spin.css">
	<link rel="stylesheet" href="/lib/dselect/1.0.4/dist/css/dselect.min.css">
	<link rel="stylesheet" href="/res/css/bootstrap-adjust.css">
	<link rel="stylesheet" href="/res/css/main.css">
	'.$styleSheets.'
	<style>
		'.$pageStyle.'
	</style>
	<script src="/res/js/theme.js"></script>
</head>
<body class="min-vh-100 vstack">
<span id="pageInfo" data-page-initted="false"></span>
'.self::getNav().'
<div id="mainContainer" class="container flex-grow-1">
	{#if showTitle}
		<h1>{#icons/pageIcon page=page addSpace=true}{/icons/pageIcon}{title}</h1>
		{#insert additionalTitleContent}{/}
			<hr/>
	{/if}
	<div id="messageDiv">
	</div>

	<main class="" role="main">
';
	}
	
	public static function getPageEnd(
	):string {
		return '
	</main>
</div>
<footer id="footer" class="container mb-3" role="contentinfo">
	<hr/>
	<div class="row">
		<div class="col-sm-4">
			<span class="h5">Open QuarterMaster Base Station</span><br/>
			Version <a href="{config:[\'service.gitLink\']}" target="_blank">{config:[\'service.version\']}</a>, &copy; {generateDatetime.getYear()} <a href="https://epic-breakfast-productions.tech/" target="_blank">EBP <img src="/media/EBP-logo-icon.svg" style="max-height:1.2em;" alt="EBP Logo"/></a><br/>
			Released under the <a href="https://github.com/Epic-Breakfast-Productions/OpenQuarterMaster/blob/main/LICENSE" target="_blank">GPL v3.0 License</a><br/>
			<div class="dropup color-modes" id="theme-picker">
				<button class="btn btn-link p-0 text-decoration-none dropdown-toggle"
						id="bd-theme"
						type="button"
						aria-expanded="false"
						data-bs-toggle="dropdown"
						data-bs-display="static">
					<span class="theme-icon-active">
						{#icons/themeAuto addSpace=true}{/icons/themeAuto}
							</span>
							<span id="bd-theme-text">
							Toggle theme
						</span>
				</button>
				<ul class="dropdown-menu" aria-labelledby="bd-theme" style="--bs-dropdown-min-width: 8rem;">
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center" data-bs-theme-value="light">
							<span class="theme-icon">
								{#icons/themeLight addSpace=true}{/icons/themeLight}
									</span>
								Light
						</button>
					</li>
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center" data-bs-theme-value="dark">
							<span class="theme-icon">
								{#icons/themeDark addSpace=true}{/icons/themeDark}
									</span>
								Dark
						</button>
					</li>
					<li>
						<button type="button" class="dropdown-item d-flex align-items-center active" data-bs-theme-value="auto">
							<span class="theme-icon">
								{#icons/themeAuto addSpace=true}{/icons/themeAuto}
									</span>
								Auto
						</button>
					</li>
					<li style=" height: 5px;">
						<button type="button" class="dropdown-item d-flex align-items-center" style="font-size: 0.15em; height: 5px;" onclick="if(typeof RealDarkMode !== \'undefined\'){ RealDarkMode.realDarkMode();}else{ script=document.createElement(\'script\');script.src = \'/res/js/realDarkMode.js\';document.head.appendChild(script);}">
							<span class="theme-icon">
								{#icons/themeDark addSpace=true}{/icons/themeDark}
									</span>
								Really Dark
								</button>
					</li>
				</ul>
			</div>
			<a href="/help">{#icons/help}{/icons/help} Help & User Guide</a><br />
									<small class="fw-lighter fst-italic text-muted">
				<div class="d-grid gap-2">
					<button class="btn btn-outline-success btn-sm" type="button" data-bs-toggle="collapse"
							data-bs-target="#pageLoadInfoCollapse" aria-expanded="false"
							aria-controls="pageLoadInfoCollapse">
								Page Loaded: <span id="pageLoadTimestamp">{generateDatetime.format(dateTimeFormatter)}</span>
									(Server time)
					</button>
				</div>
				<div class="collapse" id="pageLoadInfoCollapse">
					<div class="card card-body">
									Service id: <code class="user-select-all"
								id="traceServiceName">{config:[\'quarkus.application.name\']}</code><br/>
						Trace id: <code class="user-select-all" id="traceId">{traceId}</code>
						<!-- TODO:: link to Jaeger? -->
					</div>
				</div>
			</small>
		</div>
		<div id="serverInfo" class="col-sm-4">
			{#if config:[\'service.runBy.logo\'] != " "}
				<img src="/api/v1/media/runBy/logo" style="float:right; max-width: 30%;">
			{/if}
			{#if config:[\'service.runBy.name\'] != " "}
				<span class="h5">Run by:</span><br/>
				{config:[\'service.runBy.name\']}
			{/if}
			<br/>
			{#if config:[\'service.runBy.email\'] != " " || config:[\'service.runBy.phone\'] != " " || config:[\'service.runBy.website\'] != " "}
				<span class="h6">Contact Info:</span><br/>
				{#if config:[\'service.runBy.email\'] != " "}<a href="mailto:{config:[\'service.runBy.email\']}">{config:[\'service.runBy.email\']}</a>
					<br/>{/if}
				{#if config:[\'service.runBy.phone\'] != " "}<a href="tel:{config:[\'service.runBy.phone\']}">{config:[\'service.runBy.phone\']}</a>
					<br/>{/if}
				{#if config:[\'service.runBy.website\'] != " "}<a href="{config:[\'service.runBy.website\']}">{config:[\'service.runBy.website\']}</a>
					<br/>{/if}
			{/if}
		</div>
		<div class="col-sm-4">
			{config:[\'service.runBy.motd\']}
		</div>
	</div>
</footer>


<!-- Modals -->
{#insert modals}{/}

	<!-- scripts -->
<script src="/webjars/jquery/3.7.1/jquery.min.js"></script>
<script src="/webjars/bootstrap/5.3.2/js/bootstrap.bundle.min.js"></script>
<script src="/lib/luxon/3.3.0/luxon.min.js"></script>
<script src="/lib/js-cookie-3.0.1/js.cookie.min.js"></script>
<script src="/lib/spin.js/spin.umd.js"></script>
<script src="/lib/dselect/1.0.4/dist/js/dselect.js"></script>
<script src="/res/js/spinnerHelpers.js"></script>
<script src="/res/js/getParamUtils.js"></script>
<script src="/res/js/pageMessages.js"></script>
<script src="/res/js/dselectHelpers.js"></script>
<script src="/res/js/rest.js"></script>
<script src="/res/js/timeHelpers.js"></script>
<script src="/res/js/main.js"></script>
<script src="/res/js/icons.js"></script>
<script src="/res/js/links.js"></script>
<script>
	var popoverTriggerList = [].slice.call(document.querySelectorAll(\'[data-bs-toggle="popover"]\'));
	var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
		return new bootstrap.Popover(popoverTriggerEl)
	});
	Dselect.setupPageDselects();
</script>
{#insert scripts}{/}
	{!
{#if styleSheets??}
	{#for script in scripts}
		<script src="{script}"></script>
{/for}
{/if}
!}

	{#insert pageScript}{/}

		</body>
</html>

';
	}
	
	public static function getWholePage(
		string $content
	):string {
		return self::getPageStart() . '
' . $content . '
' . self::getPageEnd();
	}
}