<%@ include file="/include.jsp" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="hubConfigPersistenceManager" type="com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager" scope="request" />

<bs:linkScript>
    /js/bs/testConnection.js
</bs:linkScript>

<c:url var="controllerUrl" value="/admin/hub/serverHubConfigTab.html" />

<style type="text/css">
    .dialogDetails {
        font-family: 'Menlo', 'Bitstream Vera Sans Mono', 'Courier New', 'Courier', monospace;
        padding: 5px;
        border: 1px solid #ccc;
        font-size: 12px;
    }
    .textFieldLong {
        width: 100%;
        display: inline-block;
    }
    .label {
        width: 200px;
        display: inline-block;
    }
</style>

<script type="text/javascript">
    var EnableFormAgain = function() {
    }

    var TestConnectionDialog = OO.extend(BS.AbstractPasswordForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function() {
            return $('testConnectionDialog');
        },
        showTestDialog: function(successful, connectionDetails) {
            if (successful) {
                $('testConnectionStatus').innerHTML = 'Connection successful!';
                $('testConnectionStatus').className = 'testConnectionSuccess';
            } else {
                $('testConnectionStatus').innerHTML = 'Connection failed!';
                $('testConnectionStatus').className = 'testConnectionFailed';
            }
            $('testConnectionDetails').innerHTML = connectionDetails;
            $('testConnectionDetails').style.height = '';
            $('testConnectionDetails').style.overflow = 'auto';
            this.showCentered();
        },
        testConnection: function() {
            var that = this;

            // will serialize form params, and submit form to form.action
            // if XML with errors is returned, corresponding error listener methods will be called
            BS.FormSaver.save(this, $('bdHubForm').action + '?testConnection=true', OO.extend(BS.ErrorsAwareListener, {
                errorUrl: function(elem) {
                    $('errorUrl').innerHTML = elem.firstChild.nodeValue;
                },
                errorTimeout: function(elem) {
                    $('errorTimeout').innerHTML = elem.firstChild.nodeValue;
                },
                errorUserName: function(elem) {
                    $('errorUserName').innerHTML = elem.firstChild.nodeValue;
                },
                errorPassword: function(elem) {
                    $('errorPassword').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyServer: function(elem) {
                    $('errorHubProxyServer').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyPort: function(elem) {
                    $('errorHubProxyPort').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubNoProxyHost: function(elem) {
                    $('errorHubNoProxyHost').innerHTML = elem.firstChild.nodeValue;
                },
                errorConnection: function(elem) {
                    TestConnectionDialog.showTestDialog(false, elem.firstChild.nodeValue);
                },
                onSuccessfulSave: function() {
                    TestConnectionDialog.showTestDialog(true,
                        'Successful Connection');
                    // Need to enable the form again, the AbstractPasswordForm disables it by default.
                    that.enable();
                }
            }), false);

            return false;
        }
    }));

    var Config = OO.extend(BS.AbstractPasswordForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function() {
            return $('savingDialog');
        },
        savingIndicator: function() {
            return $('saving');
        },
        showConfigDialog: function(successful, savingDetails) {
            if (successful) {
                $('savingStatus').innerHTML = 'Saving successful!';
                //Use testConnection css class
                $('savingStatus').className = 'testConnectionSuccess';
            } else {
                $('savingStatus').innerHTML = 'Saving failed!';
                //Use testConnection css class
                $('savingStatus').className = 'testConnectionFailed';
            }
            $('savingDetails').innerHTML = savingDetails;
            $('savingDetails').style.height = '';
            $('savingDetails').style.overflow = 'auto';
            this.showCentered();
        },

        load: function() {
            $('hubUrl').value = "${hubConfigPersistenceManager.configuredServer.getHubUrl()}";
            $('hubUser').value = "${hubConfigPersistenceManager.configuredServer.globalCredentials.getHubUser()}";
            $('hubPass').value = "${hubConfigPersistenceManager.configuredServer.globalCredentials.getMaskedPassword()}";
            $('hubTimeout').value = "${hubConfigPersistenceManager.configuredServer.getHubTimeout()}";
            $('hubProxyServer').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getHost()}";
            $('hubProxyPort').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getPort()}";
            $('hubNoProxyHost').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getIgnoredProxyHosts()}";
            $('hubProxyUser').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getProxyUsername()}";
            $('hubProxyPass').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getProxyPassword()}";
        },

        save: function() {
            var that = this;
            // will serialize form params, and submit form to form.action
            // if XML with errors is returned, corresponding error listener methods will be called
            BS.FormSaver.save(this, $('bdHubForm').action + '?saving=true', OO.extend(BS.ErrorsAwareListener, {
                load: function() {
                    $('hubUrl').value = "${hubConfigPersistenceManager.configuredServer.getHubUrl()}";
                    $('hubUser').value = "${hubConfigPersistenceManager.configuredServer.globalCredentials.getHubUser()}";
                    $('hubPass').value = "${hubConfigPersistenceManager.configuredServer.globalCredentials.getMaskedPassword()}";
                    $('hubTimeout').value = "${hubConfigPersistenceManager.configuredServer.getHubTimeout()}";
                    $('hubProxyServer').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getHost()}";
                    $('hubProxyPort').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getPort()}";
                    $('hubNoProxyHost').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getIgnoredProxyHosts()}";
                    $('hubProxyUser').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getProxyUsername()}";
                    $('hubProxyPass').value = "${hubConfigPersistenceManager.configuredServer.getProxyInfo().getProxyPassword()}";
                },
                errorUrl: function(elem) {
                    $('errorUrl').innerHTML = elem.firstChild.nodeValue;
                },
                errorTimeout: function(elem) {
                    $('errorTimeout').innerHTML = elem.firstChild.nodeValue;
                },
                errorUserName: function(elem) {
                    $('errorUserName').innerHTML = elem.firstChild.nodeValue;
                },
                errorPassword: function(elem) {
                    $('errorPassword').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyServer: function(elem) {
                    $('errorHubProxyServer').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyPort: function(elem) {
                    $('errorHubProxyPort').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubNoProxyHost: function(elem) {
                    $('errorHubNoProxyHost').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyUser: function(elem) {
                    $('errorHubProxyUser').innerHTML = elem.firstChild.nodeValue;
                },
                errorHubProxyPass: function(elem) {
                    $('errorHubProxyPass').innerHTML = elem.firstChild.nodeValue;
                },
                errorSaving: function(elem) {
                    Config.showConfigDialog(false, elem.firstChild.nodeValue);
                },
                onSuccessfulSave: function() {
                    Config.showConfigDialog(true, 'Saving was successful.');
                    // Need to enable the form again, the AbstractPasswordForm disables it by default.
                    that.enable();
                }
            }), false);
            return false;
        }
    }));

    $j(document).ready(function() {
        Config.load();
    });
</script>

<div id="bdHubContainer">
    <form id="bdHubForm" action="/admin/hub/serverHubConfigTab.html" autocomplete="off">
        <h3>Hub Settings</h3>
        <table border="0" style="width: 100%">
            <tr>
                <td width="200px">
                    <label class="label" for="hubUrl">Server URL:
                        <span class="mandatoryAsterix" title="Mandatory field">*</span>
                        <bs:helpIcon iconTitle="Specify the URL of your Hub installation, for example: http://hub.blackducksoftware" />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubUrl" id="hubUrl" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorUrl" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubUser">User Name:
                        <span class="mandatoryAsterix" title="Mandatory field">*</span>
                        <bs:helpIcon iconTitle="Hub user name." />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubUser" id="hubUser" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorUserName" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubPass">User Password:
                        <bs:helpIcon iconTitle="Password of the user entered above." />
                    </label>
                </td>
                <td>
                    <forms:passwordField className="textFieldLong" name="hubPass" id="hubPass" />
                </td>
            </tr>
            <tr>
                <td>
                </td>
                <td>
                    <span style="margin-left:0 !important" class="smallNote">Only enter a password if you want to change the saved password</span>
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorPassword" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px" >
                    <label class="label" for="hubTimeout">Timeout (secs):
                    <span class="mandatoryAsterix" title="Mandatory field">*</span>
                        <bs:helpIcon
                                iconTitle="Hub connection timeout."/>
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubTimeout" id="hubTimeout" />
                </td>
            </tr>
             <tr>
                <td/>
                <td>
                    <span class="error" id="errorTimeout" style="margin-left: 0;"></span>
                </td>
            </tr>
        </table>
        <h3>Proxy Settings</h3>
        <table border="0" style="width: 100%">
            <tr>
                <td width="200px">
                    <label class="label" for="hubProxyServer">Proxy Host Name:
                        <bs:helpIcon iconTitle="If the TeamCity server is behind a firewall and does not have direct access to the internet, you may want to specify a proxy server. This will send any requests from the Hub Plugin to this server first." />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubProxyServer" id="hubProxyServer" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorHubProxyServer" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubProxyPort">Proxy Port:
                        <bs:helpIcon iconTitle="The port to be used to connect to the Proxy Server" />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubProxyPort" id="hubProxyPort" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorHubProxyPort" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubNoProxyHost">No Proxy Host Names:
                        <bs:helpIcon iconTitle="Specify host name regular expression patterns that shouldn't go through the proxy, in a comma separated list. Ex. .*blackducksoftware.com.*" />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubNoProxyHost" id="hubNoProxyHost" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorHubNoProxyHost" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubProxyUser">
                        Proxy Username:
                        <bs:helpIcon iconTitle="The username to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication." />
                    </label>
                </td>
                <td>
                    <forms:textField className="textFieldLong" name="hubProxyUser" id="hubProxyUser" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorHubProxyUser" style="margin-left: 0;"></span>
                </td>
            </tr>
            <tr>
                <td width="200px">
                    <label class="label" for="hubProxyPass">
                        Proxy Password:
                        <bs:helpIcon iconTitle="The password to use in the Proxy authentication. We currently only support proxies with Basic authenticaiton or no authentication." />
                    </label>
                </td>
                <td>
                    <forms:passwordField className="textFieldLong" name="hubProxyPass" id="hubProxyPass" />
                </td>
            </tr>
            <tr>
                <td/>
                <td>
                    <span class="error" id="errorHubProxyPass" style="margin-left: 0;"></span>
                </td>
            </tr>
        </table>

        <div class="saveButtonsBlock" id="saveButtonsBlock">
            <input type="submit" value="Save" id=saveButton class="btn btn_primary submitButton" onclick="Config.save();"></input>
            <input type="button" value="Test connection" class="btn btn_primary submitButton" id="testConnection" onclick="TestConnectionDialog.testConnection();"></input>
            <img id="saving" style="display: none; " class="progressRing progressRingDefault" src="/img/ajax-loader.gif" width="16" height="16" alt="Please wait..." title="Please wait..." />
            <input type="hidden" id="publicKey" name="publicKey" value="<c:out value='${hubConfigPersistenceManager.hexEncodedPublicKey}'/>" />
        </div>
    </form>
</div>

<div id="testConnectionDialog" class=" modalDialog">
    <div class="dialogHeader">
        <div class="closeWindow">
            <a title="Close dialog window" onclick="BS.TestConnectionDialog.close();" showdiscardchangesmessage='false'>
                <img src='/img/close.gif' />
            </a>
        </div>
        <div class="dialogHandle">
            <h3 class="dialogTitle" id="">Test Connection</h3>
        </div>
    </div>

    <div class="modalDialogBody">
        <div id="testConnectionStatus"></div>
        <div id="testConnectionDetails" class="dialogDetails"></div>
    </div>
</div>

<div id="savingDialog" class=" modalDialog">
    <div class="dialogHeader">
        <div class="closeWindow">
            <a title="Close dialog window" onclick="Config.close();" showdiscardchangesmessage='false'>
                <img src='/img/close.gif' />
            </a>
        </div>
        <div class="dialogHandle">
            <h3 class="dialogTitle" id="">Saving</h3>
        </div>
    </div>

    <div class="modalDialogBody">
        <div id="savingStatus"></div>
        <div id="savingDetails" class="dialogDetails"></div>
    </div>
</div>
