ScriptedPlugin.scriptedSettings

scriptedLaunchOpts ++= {
  Seq("-Xmx1024M",
    "-XX:MaxPermSize=256M",
    "-Dplugin.version=" + version.value,
    "-Dsbt.version=" + (sbtVersion in pluginCrossBuild).value)
}

scriptedBufferLog := false
