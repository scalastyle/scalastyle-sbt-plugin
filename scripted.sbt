ScriptedPlugin.scriptedSettings

scriptedLaunchOpts <++= version apply { version =>
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version)
}

scriptedBufferLog := false
