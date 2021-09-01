package graphics.scenery.tests.examples.basic

import org.joml.Vector3f
import graphics.scenery.*
import graphics.scenery.attribute.material.DefaultMaterial
import graphics.scenery.attribute.material.Material
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

        val scalingFactor = 0.5f
        val atomicSimulation = AtomicSimulation.fromCube("/home/fiedlerl/data/qe_calcs/Be128/1560K/MD_with_charge_density_02/" +
            "Be_dens0000.cube", hub, scalingFactor, 0.5f, normalizeVolumetricDataTo = 0.2f,
            cubeStyle = "QE")
        scene.addChild(atomicSimulation)

        // Customize the atomic simulation.
        val atomicMaterial = DefaultMaterial.Material()
        atomicMaterial.metallic = 0.3f
        atomicMaterial.roughness = 0.6f
        atomicMaterial.diffuse = Vector3f(0.7f, 0.5f, 0.5f)
        atomicSimulation.updateAtomicMaterial(atomicMaterial)
        atomicSimulation.volumetricData.colormap = Colormap.get("viridis")
        atomicSimulation.volumetricData.transferFunction = TransferFunction.ramp(0.1f, 0.2f, 0.5f)


        // One light in every corner.
        val lights = (0 until 8).map {
            PointLight(radius = 60.0f*scalingFactor)
        }
        lights.mapIndexed { i, light ->
            val permutation = String.format("%3s", Integer.toBinaryString(i)).replace(' ', '0')
            light.spatial().position = Vector3f(atomicSimulation.simulationData.unitCellDimensions[0] * (permutation[0].code-48) ,
                atomicSimulation.simulationData.unitCellDimensions[1] * (permutation[1].code-48),
                atomicSimulation.simulationData.unitCellDimensions[2] * (permutation[2].code-48))
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
        thread {
            while (running) {
                // Read new MD snapshot.
                val snapshotNumber = currentSnapshot.toString().padStart(4, '0')
                atomicSimulation.updateFromCube("/home/fiedlerl/data/qe_calcs/Be128/1560K/MD_with_charge_density/" +
                        "Be_dens${snapshotNumber}.cube")
                Thread.sleep(33)
                currentSnapshot++
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

