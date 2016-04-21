/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
var filteredSecurityClassName = " rowFilteredSecurity";
var filteredLicenseClassName = " rowFilteredLicense";
var filteredOperationalClassName = " rowFilteredOperational";

var tableId = "hubBomReport";

var highSecurityColumnNum = 3;
var mediumSecurityColumnNum = 4;
var lowSecurityColumnNum = 5;
var licenseRiskColumnNum = 6;
var operationRiskColumnNum = 7;

function adjustWidth(object) {
	var percentageSpan = object.getElementsByTagName("SPAN")[0];
	var percent = percentageSpan.innerHTML;
	percentageSpan.style.display = "none";
	object.style.width = percent;
}

function adjustTable() {
	var riskReportTable = document.getElementById(tableId).tBodies[0];
	var odd = true;
	for (var i = 0; i < riskReportTable.rows.length; i++) {
		if (riskReportTable.rows[i].className
				.indexOf(filteredSecurityClassName) != -1) {
			continue;
		}
		if (riskReportTable.rows[i].className.indexOf(filteredLicenseClassName) != -1) {
			continue;
		}
		if (riskReportTable.rows[i].className
				.indexOf(filteredOperationalClassName) != -1) {
			continue;
		}
		adjustTableRow(riskReportTable.rows[i], odd);
		adjustSecurityRisks(riskReportTable.rows[i]);
		adjustOtherRisks(riskReportTable.rows[i], licenseRiskColumnNum);
		adjustOtherRisks(riskReportTable.rows[i], operationRiskColumnNum);
		odd = !odd;
	}
}

function adjustTableRow(row, odd) {
	var className = row.className;

	if (odd) {
		if (!className || className.length == 0) {
			className += "oddRow";
		} else {
			if (className.indexOf("evenRow") != -1) {
				className = className.replace("evenRow", "oddRow");
			}
		}
	} else {
		if (!className || className.length == 0) {
			className += "evenRow";
		} else {
			if (className.indexOf("oddRow") != -1) {
				className = className.replace("oddRow", "evenRow");
			}
		}
	}

	row.className = className;
}

function adjustSecurityRisks(row) {
	if (row.cells[highSecurityColumnNum].children[0].innerHTML > 0) {
		if (row.cells[highSecurityColumnNum].children[0].className
				.indexOf("security-risk-high-count") == -1) {
			row.cells[highSecurityColumnNum].children[0].className += " security-risk-high-count";
		}
	}
	if (row.cells[mediumSecurityColumnNum].children[0].innerHTML > 0) {
		if (row.cells[mediumSecurityColumnNum].children[0].className
				.indexOf("security-risk-med-count") == -1) {
			row.cells[mediumSecurityColumnNum].children[0].className += " security-risk-med-count";
		}
	}
	if (row.cells[lowSecurityColumnNum].children[0].innerHTML > 0) {
		if (row.cells[lowSecurityColumnNum].children[0].className
				.indexOf("security-risk-low-count") == -1) {
			row.cells[lowSecurityColumnNum].children[0].className += " security-risk-low-count";
		}
	}
}

function adjustOtherRisks(row, riskColumnNum) {
	if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") != -1) {
		if (row.cells[riskColumnNum].children[0].className
				.indexOf("security-risk-high-count") == -1) {
			row.cells[riskColumnNum].children[0].className += " security-risk-high-count";
		}
	}
	if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") != -1) {
		if (row.cells[riskColumnNum].children[0].className
				.indexOf("security-risk-med-count") == -1) {
			row.cells[riskColumnNum].children[0].className += " security-risk-med-count";
		}
	}
	if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") != -1) {
		if (row.cells[riskColumnNum].children[0].className
				.indexOf("security-risk-low-count") == -1) {
			row.cells[riskColumnNum].children[0].className += " security-risk-low-count";
		}
	}
}

function filterTableByVulnerabilityRisk(risk) {
	filterTableByRisk(risk, 'highSecurityRiskLabel', 'mediumSecurityRiskLabel',
			'lowSecurityRiskLabel', 'noneSecurityRiskLabel',
			filteredSecurityClassName);
}

function filterTableByLicenseRisk(risk) {
	filterTableByRisk(risk, 'highLicenseRiskLabel', 'mediumLicenseRiskLabel',
			'lowLicenseRiskLabel', 'noneLicenseRiskLabel',
			filteredLicenseClassName);
}

function filterTableByOperationalRisk(risk) {
	filterTableByRisk(risk, 'highOperationalRiskLabel',
			'mediumOperationalRiskLabel', 'lowOperationalRiskLabel',
			'noneOperationalRiskLabel', filteredOperationalClassName);
}

function filterTableByRisk(risk, highRiskId, mediumRiskId, lowRiskId,
		noneRiskId, filterClassName) {
	if (removeFilter(highRiskId, risk, filterClassName)) {
		return;
	}
	if (removeFilter(mediumRiskId, risk, filterClassName)) {
		return;
	}
	if (removeFilter(lowRiskId, risk, filterClassName)) {
		return;
	}
	if (removeFilter(noneRiskId, risk, filterClassName)) {
		return;
	}
	risk.className += " filterSelected";
	filterTable(document.getElementById(tableId).tBodies[0], risk, false,
			filterClassName);
	adjustTable();
}

function removeFilter(id, currRisk, filterClassName) {
	var riskLabel = document.getElementById(id);
	if (riskLabel.className.indexOf(" filterSelected") != -1) {
		filterTable(document.getElementById(tableId).tBodies[0], null, true,
				filterClassName);
		document.getElementById(id).className = document.getElementById(id).className
				.replace(' filterSelected', '');

		adjustTable();
		if (id == currRisk.id) {
			return true;
		}
	}

}

function filterTable(riskReportTable, riskToFilter, shouldRemoveFilter,
		filterClassName) {
	var odd = true;
	for (var i = 0; i < riskReportTable.rows.length; i++) {
		if (shouldRemoveFilter) {
			removeFilterFromRow(riskReportTable.rows[i], filterClassName);
		} else {
			if (filterClassName == filteredSecurityClassName) {
				filterRowBySecurity(riskReportTable.rows[i], riskToFilter,
						filterClassName);
			} else if (filterClassName == filteredLicenseClassName) {
				filterRowByOtherRisk(riskReportTable.rows[i], riskToFilter,
						filterClassName, licenseRiskColumnNum);
			} else if (filterClassName == filteredOperationalClassName) {
				filterRowByOtherRisk(riskReportTable.rows[i], riskToFilter,
						filterClassName, operationRiskColumnNum);
			}
		}
		adjustTableRow(riskReportTable.rows[i], odd);
		odd = !odd;
	}
}

function filterRowBySecurity(row, riskToFilter, filterClassName) {
	if (riskToFilter.id.indexOf("none") != -1) {
		// only show the rows that have no security risks
		if (row.cells[highSecurityColumnNum].children[0].innerHTML != 0
				|| row.cells[mediumSecurityColumnNum].children[0].innerHTML != 0
				|| row.cells[lowSecurityColumnNum].children[0].innerHTML != 0) {
			filterRowByRisk(row, filterClassName);
		}
	} else if (riskToFilter.id.indexOf("high") > -1) {
		// only show the rows that have high security risks
		if (row.cells[highSecurityColumnNum].children[0].innerHTML == 0) {
			filterRowByRisk(row, filterClassName);
		}
	} else if (riskToFilter.id.indexOf("medium") > -1) {
		// only show the rows that have medium security risks without high risks
		// if the component has a high security risk then it is not included in
		// the medium risk components
		if (row.cells[highSecurityColumnNum].children[0].innerHTML != 0
				|| row.cells[mediumSecurityColumnNum].children[0].innerHTML == 0) {
			filterRowByRisk(row, filterClassName);
		}
	} else if (riskToFilter.id.indexOf("low") > -1) {
		// only show the rows that have low security risks without high or
		// medium risks
		// if the component has a high or medium security risk then it is not
		// included in the low risk components
		if ((row.cells[highSecurityColumnNum].children[0].innerHTML != 0 && row.cells[mediumSecurityColumnNum].children[0].innerHTML != 0)
				|| row.cells[lowSecurityColumnNum].children[0].innerHTML == 0) {
			filterRowByRisk(row, filterClassName);
		}
	}
}

function filterRowByRisk(row, filterClassName) {
	if (row.className.indexOf(filterClassName) == -1) {
		row.className += filterClassName;
	}
}

function filterRowByOtherRisk(row, riskToFilter, filterClassName, riskColumnNum) {
	if (riskToFilter.id.indexOf("none") != -1) {
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") != -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") != -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") != -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
	} else if (riskToFilter.id.indexOf("high") > -1) {
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("H") == -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
	} else if (riskToFilter.id.indexOf("medium") > -1) {
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("M") == -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
	} else if (riskToFilter.id.indexOf("low") > -1) {
		if (row.cells[riskColumnNum].children[0].innerHTML.indexOf("L") == -1) {
			if (row.className.indexOf(filterClassName) == -1) {
				row.className += filterClassName;
			}
		}
	}
}

function removeFilterFromRow(row, filterClassName) {
	if (row.className.indexOf(filterClassName) != -1) {
		row.className = row.className.replace(filterClassName, "");
	}
}
