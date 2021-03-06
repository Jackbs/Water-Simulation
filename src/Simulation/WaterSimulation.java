package Simulation;

import java.util.*;

/**
 * Created by Jack on 23-07-16.
 * Unused for now
 */
public class WaterSimulation{
    private Level workingLevel;
    private Set WaterGroup1 = new HashSet<BlockLocation>();
    private Set WaterBlocksPos;
    private List<FluidBlock> WaterBlocks;
    private List WaterSortedByP = new ArrayList<FluidBlock>();
    private double Atmosphericpressure = 101.325;
    private final double kpaPerBlock = 9.807;
    public int tick = 0;


    WaterSimulation(){
        Atmosphericpressure = 0.0;
    }

    public Level doWaterSimTick(Level ThisLevel){
        WaterBlocks = new ArrayList<>();
        WaterBlocks.clear();
        workingLevel = ThisLevel;
        WaterBlocksPos = ThisLevel.WaterBlockPos;


        System.out.println("Doing Sim tick["+tick+"]");

        Iterator iter = workingLevel.WaterBlockPos.iterator();
        BlockLocation highestliq = new BlockLocation(0,0,0,null);

        while (iter.hasNext()) {
            BlockLocation blkloc = ((BlockLocation)iter.next());
            if(highestliq.z<blkloc.z){
                highestliq = blkloc;
            }
        }
        if(highestliq.p == null){
            System.out.println("Could not start simulation, no water blocks found");
            return workingLevel;
        }
        //System.out.println("Highest Block info:"+highestliq.stringBlockInfomation(workingLevel));

        //updateDepth(highestliq,0.0);

        //System.out.println("updatePressure done, numblocks: "+WaterGroup1.size());

        //FluidBlock fb1 = (FluidBlock)workingLevel.getBlock(highestliq);
        //double eval = (fb1.calcEvalue(1));

        double filllevel = 0.0;

        //Get Pressure at bottem from height

        refreshWaterBlocks();

        for (FluidBlock fb : WaterBlocks) {
            fb.updateSideBlocks();
            fb.findPressureFromH();
        }

        for (FluidBlock fb : WaterBlocks) {
            fb.findoutwardEnergy();
        }

        for (FluidBlock fb : WaterBlocks) {
            fb.findoutwardVelosity();
        }

        for (FluidBlock fb : WaterBlocks) {
            fb.OutputWater();
        }

        refreshWaterBlocks(); //Output of water can create new blocks, so need to referesh set of water blocks

        for (FluidBlock fb : WaterBlocks) {
            try {
                fb.EvaluateInternalFlow();
            } catch (Exception e){
                System.out.println("Error when eval internal flow");
                fb.printAllData();
                e.printStackTrace();
            }

            filllevel = filllevel + fb.getFillLevel();
        }

        System.out.println("Total Level:"+filllevel);


        /*
        //Put fluidBlocks into list based on there block locations, as well as calculating the pressure of them releitive to the highest block
        for (Object b : WaterGroup1) {
            FluidBlock fb = ((FluidBlock)workingLevel.getBlock((BlockLocation)b));
            fb.calcPressure(0.0,0); //Calculate Pressure relitive to the highest block
            //fb.printAllData();
            WaterSortedByP.add(fb);
        }

        //Calculate the MaxPressure for each block, so it does not have to be calculated each time it is compeared in sorting
        for(int i = 0;i<WaterSortedByP.size();i++){
            ((FluidBlock)WaterSortedByP.get(i)).calcMaxPressure();
        }

        Collections.sort(WaterSortedByP, new MaxPComparator());

        for(int i = 0;i<WaterSortedByP.size();i++){
            ((FluidBlock)WaterSortedByP.get(i)).printAllData();
        }
        */





        tick++;
        return workingLevel;
    }

    public void refreshWaterBlocks(){
        WaterBlocks.clear();

        for (Object b : WaterBlocksPos) {
            FluidBlock fb = (FluidBlock) workingLevel.getBlock((BlockLocation) b);
            WaterBlocks.add(fb);
        }
    }

    public void updateDepth(BlockLocation blkloc,double topdepth){

        if(WaterGroup1.contains(blkloc)){ //FluidBlock has allready been added
            return;
        }else{
            WaterGroup1.add(blkloc);
        }
        //System.out.println("Doing Recessive P set on: "+blkloc.stringBlockInfomation(workingLevel));

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, 1)) == null)||(workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, 1)).getId() == 0)){ //There is air or null above the block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(0.0);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, 1)) != null)&& (workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, 1)).getId() == 5)){ //There is a water block above this block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(1, 0, 0)) != null)&&((workingLevel.getBlock(blkloc.offsetBlkLoc(1, 0, 0)).getId() == 5))){ //There is a water to the right of the block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
            updateDepth(blkloc.offsetBlkLoc(1, 0, 0),topdepth);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 1, 0)) != null)&&((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 1, 0)).getId() == 5))){ //There is a water to the up of the block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
            updateDepth(blkloc.offsetBlkLoc(0, 1, 0), topdepth);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(-1, 0, 0)) != null)&&((workingLevel.getBlock(blkloc.offsetBlkLoc(-1, 0, 0)).getId() == 5))){ //There is a water to the left of the block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
            updateDepth(blkloc.offsetBlkLoc(-1, 0, 0), topdepth);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(0, -1, 0)) != null)&&((workingLevel.getBlock(blkloc.offsetBlkLoc(0, -1, 0)).getId() == 5))){ //There is a water to the down of the block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
            updateDepth(blkloc.offsetBlkLoc(0, -1, 0), topdepth);
        }

        if((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, -1)) != null)&&((workingLevel.getBlock(blkloc.offsetBlkLoc(0, 0, -1)).getId() == 5))){ //There is a water block bellow this block
            FluidBlock thisFluidBlock = (FluidBlock)workingLevel.getBlock(blkloc);
            thisFluidBlock.setDepth(topdepth);
            updateDepth(blkloc.offsetBlkLoc(0, 0, -1), topdepth+1);
        }
    }

    class MaxPComparator implements Comparator<FluidBlock> {
        public int compare(FluidBlock f1, FluidBlock f2) {
            if (f1.getMaxPressure() < f2.getMaxPressure()) return 1;
            if (f1.getMaxPressure() > f2.getMaxPressure()) return -1;
            return 0;
        }
    }

    public void Reset(){
        tick = 0;
    }
}
