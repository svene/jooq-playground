import org.testcontainers.utility.ResourceReaper

//containerId = "${testcontainer.containerid}"
//imageName = "${testcontainer.imageName}"
containerId = properties['testcontainer.containerid']
imageName = properties['testcontainer.imageName']
println("Stopping testcontainer $containerId - $imageName")
ResourceReaper.instance().stopAndRemoveContainer(containerId, imageName);
