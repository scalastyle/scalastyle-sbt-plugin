ScriptedPlugin.scriptedSettings

scriptedLaunchOpts ++= {
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false
