package graphics.scenery.tests.examples.basic

import org.joml.Vector3f
import graphics.scenery.*
import graphics.scenery.utils.extensions.*
import graphics.scenery.backends.Renderer
import graphics.scenery.volumes.Colormap
import graphics.scenery.volumes.TransferFunction
import graphics.scenery.volumes.Volume
import net.imglib2.type.numeric.integer.UnsignedByteType
import kotlin.concurrent.thread

/**
 * <Description>
 *  Visualizes a DFT trajectory.
 *
 * @author Lenz Fiedler <l.fiedler@hzdr.de>
 */
class DFTMDVisualizationCASUSOpenDay : SceneryBase("DFTExample", wantREPL = System.getProperty("scenery.master", "false").toBoolean()) {
    override fun init() {
        renderer = hub.add(SceneryElement.Renderer,
            Renderer.createRenderer(hub, applicationName, scene, 512, 512))
        val snapshot = DFTParser(normalizeDensityTo= 0.2f)
        snapshot.parseCube("/home/fiedlerl/data/qe_calcs/Be128/1560K/MD_with_charge_density_02/" +
            "Be_dens0000.cube")

        // Scales the DFT coordinates (which are in Bohr units) for a better VR experience.
        val scalingFactor = 0.5f

        // Visualize the atoms.
        val atomicRadius = 0.5f*scalingFactor
        val atoms: Array<Icosphere> = Array<Icosphere>(snapshot.numberOfAtoms) {Icosphere(atomicRadius, 4)}
        atoms.zip(snapshot.atomicPositions).forEach {
            with(it.component1()){
                spatial{
                    position = scalingFactor * it.component2()
                }
                material{
                    metallic = 0.3f
                    roughness = 0.6f
                    diffuse = Vector3f(0.7f, 0.5f, 0.5f)
                }
            }
            scene.addChild(it.component1())
        }

        // Visualize the density data.
        val volume = Volume.fromBuffer(emptyList(), snapshot.gridDimensions[0], snapshot.gridDimensions[1],
                                        snapshot.gridDimensions[2], UnsignedByteType(), hub)

        volume.name = "volume"
        // Note: Volumes centered at the origin are currently offset by -2.0 in each direction
        // (see Volume.kt, line 338), so we're adding 2.0 here.
        volume.spatial().position = (scalingFactor * Vector3f(snapshot.unitCellOrigin[0],snapshot.unitCellOrigin[1],
            snapshot.unitCellOrigin[2])).add(
            Vector3f(2.0f, 2.0f, 2.0f))
        volume.colormap = Colormap.get("viridis")
        volume.pixelToWorldRatio = snapshot.gridSpacings[0]*scalingFactor
        volume.transferFunction = TransferFunction.ramp(0.1f, 0.2f, 0.5f)
        scene.addChild(volume)


        // One light in every corner.
        val lights = (0 until 8).map {
            PointLight(radius = 60.0f*scalingFactor)
        }
        lights.mapIndexed { i, light ->
            val permutation = String.format("%3s", Integer.toBinaryString(i)).replace(' ', '0')
            light.spatial().position = Vector3f(snapshot.unitCellDimensions[0] * (permutation[0].code-48) ,
                                      snapshot.unitCellDimensions[1] * (permutation[1].code-48),
                                      snapshot.unitCellDimensions[2] * (permutation[2].code-48))
            light.emissionColor = Vector3f(1.0f, 1.0f, 1.0f)
            light.intensity = 1.0f
            scene.addChild(light)
        }

        val cam: Camera = DetachedHeadCamera()
        with(cam) {
            spatial {
                position = Vector3f(0.0f, 0.0f, 5.0f)
            }
            perspectiveCamera(50.0f, 512, 512)

            scene.addChild(this)
        }

        // Don't start with the first crystalline snapshots, the electronic density
        // is very localized in that ones.
        val firstSnapshot = 100
        var currentSnapshot = firstSnapshot
        val maxSnapshot = 5932
        var count = 0
        thread {
            while (running) {
                // Read new MD snapshot.
                val snapshotNumber = currentSnapshot.toString().padStart(4, '0')
                snapshot.parseCube("/home/fiedlerl/data/qe_calcs/Be128/1560K/MD_with_charge_density/" +
                    "Be_dens${snapshotNumber}.cube")

                // Visualize the atoms.
                atoms.zip(snapshot.atomicPositions).forEach {
                    it.component1().spatial().position = scalingFactor * it.component2()

                }
                volume.addTimepoint("t-${count}", snapshot.electronicDensityUByte)
                volume.goToLastTimepoint()
                volume.purgeFirst(10, 10)
                Thread.sleep(33)
                currentSnapshot++
                count++
                if (currentSnapshot == maxSnapshot)
                {
                    currentSnapshot = firstSnapshot
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            DFTMDVisualizationCASUSOpenDay().main()
        }
    }
}

