import React, { useState } from 'react';
import { EyeOff, Eye } from 'lucide-react';

export default function App() {
  const [isHidden, setIsHidden] = useState(false);

  const toggleHide = () => {
    setIsHidden(!isHidden);
  };

  return (
    <div className="min-h-screen bg-[#191919] text-[#EBEBEB] flex flex-col items-center justify-between p-6 sm:p-10 font-sans select-none">
      
      {/* Top Flat Shizuku Status Pill */}
      <div className="pt-8">
        <div className="flex items-center gap-2 bg-[#202020] border border-[#2F2F2F] px-3.5 py-1.5 rounded-full text-xs text-[#9B9A97] font-medium cursor-pointer">
          <span className="w-2 h-2 rounded-full bg-[#4DAB9A]" />
          <span className="text-[#EBEBEB]">Shizuku Ready</span>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* CENTER: FLAT 1-CLICK MASTER BUTTON */}
      {/* ========================================================================= */}
      <div className="flex flex-col items-center space-y-6 my-auto">
        <button
          onClick={toggleHide}
          className={`w-52 h-52 rounded-full border flex flex-col items-center justify-center transition-colors cursor-pointer active:scale-95 ${
            isHidden
              ? 'bg-[#261A1A] border-[#7F2D2D] text-[#FF8585]'
              : 'bg-[#1A2330] border-[#2B4C7E] text-[#6CA8E8]'
          }`}
        >
          {isHidden ? (
            <EyeOff className="w-14 h-14 mb-2" />
          ) : (
            <Eye className="w-14 h-14 mb-2" />
          )}
          <span className="text-xl font-bold tracking-wider">
            {isHidden ? 'HIDDEN' : 'VISIBLE'}
          </span>
        </button>

        <div className="text-center space-y-1">
          <div className={`text-sm font-medium ${isHidden ? 'text-[#FF8585]' : 'text-[#6CA8E8]'}`}>
            {isHidden ? 'Status Bar is 100% HIDDEN' : 'Status Bar is VISIBLE'}
          </div>
          <p className="text-xs text-[#787774]">
            {isHidden
              ? 'Tap button to make Status Bar VISIBLE'
              : 'Tap button to HIDE Status Bar & Icons'}
          </p>
        </div>
      </div>

      {/* Spacer for vertical balance */}
      <div className="pb-8" />

    </div>
  );
}
